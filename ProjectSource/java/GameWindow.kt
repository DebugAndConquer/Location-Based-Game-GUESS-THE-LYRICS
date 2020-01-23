package com.example.guessthelyrics

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_backpack.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.math.*

// A bounding box surrounding Bay Campus and Port Tennant
private const val MIN_LAT = 51.621198
private const val MAX_LAT = 51.619280
private const val MIN_LONG = -3.882702
private const val MAX_LONG = -3.876336
private const val EARTH_RADIUS = 6366000
// The maximum distance between player and marker to allow collection of a lyric
private const val COLLECTION_DISTANCE = 25

class GameWindow : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Get the game mode. -2 will be an error value
    private var sesId = -2
    // The objects of generated markers will be stored here
    private var markers = arrayListOf<Marker>()
    // A list of markers that are in range for collection
    private var listOfCandidates = arrayListOf<Marker>()
    // Connection issue flag
    private var flag = false

    // On Marker Click Listener
    override fun onMarkerClick(p0: Marker?) = false

    // Permission request codes
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        checkConnection()
        // Check if the app has been granted the ACCESS_FINE_LOCATION permission
        // If not - request it from the user
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //Some map setup properties for a cleaner look
        mMap.isMyLocationEnabled = true
        mMap.mapType = 1
        mMap.isBuildingsEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false
        // When the user clicks on a map the camera will stop following the user
        initMarkers()
        // Initial placement of a camera
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val cameraPosition = CameraPosition.Builder()
                    .target(currentLatLng)
                    .zoom(18f)
                    .tilt(90f)
                    .bearing(90f)
                    .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
        requestNewLocationData()
    }

    override fun onResume() {
        try {
            super.onResume()
            // If the space is again available, allow collection
            if (SqlAdapter().checkBpSize(sesId, applicationContext) < SqlAdapter().getBpCapacity(
                    sesId,
                    applicationContext
                )
            ) {
                collectBtn.text = getString(R.string.collText)
            }
        } catch (e: java.lang.Exception) {
            //Make sure GPS and Cellular are on
            checkConnection()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //Allow networking procedures in this class to communicate with the db
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classic_mode)
        // Get the current game mode
        sesId = intent.extras!!.getInt(getString(R.string.gmExtra))
        // Launch the mapView
        (supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment?)?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Handles the event when the backpack button was clicked
        val bpButton = findViewById<Button>(R.id.backpackButton)
        bpButton.setOnClickListener {
            val intent = Intent(this, Backpack::class.java)
            //Parse the game mode to a backpack screen
            intent.putExtra(getString(R.string.bpGm), sesId)
            startActivity(intent)
        }
        val collectButton = findViewById<Button>(R.id.collectBtn)
        collectButton.isEnabled = false
        collectButton.setOnClickListener {
            /*
             * When the collect button is pressed, all lyric parts that are in
             * range are deleted from the map and memory
             */
            for (value in listOfCandidates) {
                // Check rather the amount added to bp is satisfied by the capacity of bp
                if (sesId == 0) {
                    SqlAdapter().addToBackpack(value, 0, applicationContext)
                } else {
                    SqlAdapter().addToBackpack(value, 1, applicationContext)
                }
                value.remove()
                markers.remove(value)
            }
            // When the last marker is collected, a new set of markers will be generated
            if (markers.size == 0) {
                initMarkers()
                Toast.makeText(
                    this, getString(R.string.newSetOfM),
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Tell a user about his/her progress
            if (listOfCandidates.size == 1) {
                Toast.makeText(
                    this, getString(R.string.collLyric),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, getString(R.string.collLyrics, listOfCandidates.size),
                    Toast.LENGTH_SHORT
                ).show()
            }
            //Empty the list because markers were deleted from the map
            listOfCandidates.clear()
        }
    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        setUpMap()
    }

    /**
     * Puts a random number of markers, each containing a lyric part on a map
     */
    private fun initMarkers() {
        //Randomly choose how many markers are going to be created
        val iterations = (9..10).random()
        for (i in 0 until iterations) {
            val r = Random()
            // The coordinates should be generated in a Bay Campus/Port Tennant area
            val randLat = MIN_LAT + r.nextGaussian() * (MAX_LAT - MIN_LAT)
            val randLong = MIN_LONG + r.nextGaussian() * (MAX_LONG - MIN_LONG)

            val position = LatLng(randLat, randLong)
            // Create a marker with a random lyric in it and save info about it
            val lyricPart = getRandomLyric(sesId)
            val markerOpts =
                MarkerOptions()
                    .position(position)
                    .title(lyricPart[0])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.note))
            val marker = mMap.addMarker(markerOpts)
            // This tag is invisible for a user and used to identify the marker in other classes
            marker.tag = lyricPart[1]
            markers.add(marker)
        }
    }

    /**
     *  Opens a random lyric file, scrapes out a random line from
     *  it and returns it with the filename
     */
    private fun getRandomLyric(gameMode: Int): ArrayList<String> {
        val lyricsFolder: Array<String>? = if (gameMode == 0) {
            assets.list(getString(R.string.classicFolder))
        } else {
            assets.list(getString(R.string.currentFolder))
        }
        // Providing the path of a subfolder in the assets folder
        // We need a random file from that directory
        val rndFile = (lyricsFolder!!.indices).random()
        val reader: BufferedReader
        //Reading the lyric file
        reader = if (gameMode == 0) {
            BufferedReader(
                InputStreamReader
                    (
                    assets.open(getString(R.string.classicFolder1, lyricsFolder[rndFile]))
                )
            )
        } else {
            BufferedReader(
                InputStreamReader
                    (
                    assets.open(getString(R.string.currentFolder1, lyricsFolder[rndFile]))
                )
            )
        }
        // Adding all lines into the list and then taking a random element (line) from it
        val array = arrayListOf<String>()
        for (line in reader.lines()) {
            array.add(line)
        }
        val rnd = Random()
        val rndIndex = rnd.nextInt(array.size)
        var randomLine = array[rndIndex]

        //Delete the last comma if any for a cleaner look
        if (with(randomLine[randomLine.length - 1]) { equals(",") }) {
            randomLine = randomLine.replace(
                randomLine.substring(randomLine.length - 1), ""
            )
        }
        val filename = lyricsFolder[rndFile]
        val results = arrayListOf<String>()
        results.add(randomLine)
        results.add(filename)
        return results
    }

    /**
     * This method is responsible for listening to a location change and triggering
     * the mLocationCallback object in that case
     */
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // How often the location changes are tracked
        mLocationRequest.interval = 2000
        mLocationRequest.fastestInterval = 1000
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        /**
         * Each time the location changes the method will scan for collectable
         * lyric parts
         */
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val lat = mLastLocation.latitude
            val long = mLastLocation.longitude
            val lastLoc = LatLng(lat, long)

            // Check rather there are any lyrics to collect within the specified distance
            listOfCandidates.clear()
            for (value in markers) {
                if (calculateDistanceBetween(lastLoc, value.position) <= COLLECTION_DISTANCE) {
                    listOfCandidates.add(value)
                }
            }
            try {
                runOnUiThread {
                    // Allow collection when there is a lyric to collect and backpack is not full
                    collectBtn.isEnabled = listOfCandidates.size > 0
                            && (SqlAdapter().checkBpSize(
                        sesId,
                        applicationContext
                    ) < SqlAdapter().getBpCapacity(sesId, applicationContext))
                    if (SqlAdapter().checkBpSize(
                            sesId,
                            applicationContext
                        ) >= SqlAdapter().getBpCapacity(sesId, applicationContext)
                    ) {
                        collectBtn.text = getString(R.string.bpFull)
                    } else {
                        collectBtn.text = getString(R.string.collect)
                    }
                }
            } catch (e: java.lang.Exception) {
                if (!flag) {
                    checkConnection()
                }
                flag = true
            }

        }
    }

    /**
     * An implementation of a geographical formula to calculate the distance between
     * 2 points on a map in meters
     */
    private fun calculateDistanceBetween(origin: LatLng, dest: LatLng): Double {
        val lat1 = origin.latitude
        val lat2 = dest.latitude
        val lon1 = origin.longitude
        val lon2 = dest.longitude
        val latDiff = Math.toRadians(lat2 - lat1)
        val longDiff = Math.toRadians(lon2 - lon1)
        val f1 = (sin(latDiff / 2).pow(2.0) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                        sin(longDiff / 2).pow(2.0)))
        val f2 = asin(sqrt(f1)) * 2

        return f2 * EARTH_RADIUS
    }

    /**
     * Checks rather the device is connected to gps and cellular. If at least
     * one option is disabled the alert will pop-up and help navigate to a
     * setting where those options might be switched on.
     */
    private fun checkConnection(): Boolean {
        val lm = this.applicationContext.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
        val manager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var gpsEnabled = false
        var networkEnabled = false
        var cellularEnabled = false
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            cellularEnabled = manager.getNetworkCapabilities(manager.activeNetwork)
                .hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } catch (ex: Exception) {

        }
        // Create an alert if rather gps or cellular is off
        var result = true
        if (!gpsEnabled || !networkEnabled || !cellularEnabled) {
            result = false
            android.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.gpsOffAlert))
                // Navigate user to a location settings screen
                .setPositiveButton(
                    getString(R.string.openLocSetts)
                ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                // Navigate user to a cellular settings screen
                .setNegativeButton(
                    getString(R.string.openCellSetts)
                ) { _, _ ->
                    val intent = Intent()
                    intent.component = ComponentName(
                        getString(R.string.androidSetts),
                        getString(R.string.cellSetts)
                    )
                    startActivity(intent)
                }
                .show()
        }



        return result
    }
}




