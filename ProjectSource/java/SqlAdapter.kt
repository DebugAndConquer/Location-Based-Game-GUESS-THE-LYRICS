package com.example.guessthelyrics

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.Marker
import java.sql.Connection
import java.sql.DriverManager

private const val MAX_POINTS_TO_AWARD = 100

class SqlAdapter {
    fun establishConn(c: Context): Connection {
        Class.forName(c.getString(R.string.jdbcPackage)).newInstance()
        val url = c.getString(R.string.jdbcPort)
        return DriverManager.getConnection(url, c.getString(R.string.jdbcUserName), "")
    }

    fun getBackpack(conn: Connection, gm: Int, c: Context): ArrayList<LyricPart> {
        val stmt = conn.createStatement()
        val ls = arrayListOf<LyricPart>()
        val rs = stmt!!.executeQuery(c.getString(R.string.getBpSQL, gm))
        while (rs.next()) {
            ls.add(
                LyricPart(
                    rs.getInt(c.getString(R.string.id)),
                    rs.getInt(c.getString(R.string.songId)),
                    rs.getString(
                        c.getString(
                            R.string.lyricPart
                        )
                    )
                )
            )
        }
        rs.close()
        stmt.close()
        conn.close()
        return ls
    }

    fun updateListView(c: Context, result: ArrayList<LyricPart>, lv: ListView) {
        val adapter = CustomAdapter(c)
        var songId = arrayListOf<Int>()
        for (lp in result) {
            songId.add(lp.songID)
        }
        if (songId.size != 0) {
            try {
                songId = songId.distinct() as ArrayList<Int>
                songId.sort()
                result.sort()
            } catch (e: ClassCastException) {
                val temp = songId[0]
                songId = arrayListOf()
                songId.add(temp)
                result.sort()
            } finally {
                adapter.createHeader(c.getString(R.string.unkSong))
                for (i in 0 until songId.size) {
                    for (j in 0 until result.size) {
                        if (songId[i] == result[j].songID) {
                            adapter.createItem(
                                result[j].lyrics
                            )
                        }
                    }
                    if (i != songId.size - 1) {
                        adapter.createHeader(c.getString(R.string.unkSongs, (i + 2)))
                    }
                }
                lv.adapter = adapter
            }
        } else {
            val customAdapter = CustomAdapter(c)
            lv.adapter = customAdapter
        }
    }

    fun triggerGuess(lv: ListView, gm: Int, x: TextView, c: Context) {
        lv.setOnItemClickListener { parent, _, position, _ ->
            // Create alert window
            val b = AlertDialog.Builder(parent.context)
            b.setTitle(c.getString(R.string.titlePrompt))
            val inp = EditText(parent.context)
            inp.inputType = InputType.TYPE_CLASS_TEXT
            b.setView(inp)
            b.setPositiveButton(c.getString(R.string.guessButt)) { _, _ ->
                val conn = establishConn(c)
                val stmt = conn.createStatement()
                val rs = stmt.executeQuery(
                    c.getString(R.string.triggerGuessSQL)
                            + inp.text.toString().replace("'", "''") + "'"
                )

                var count = 0
                var id = 0
                while (rs.next()) {
                    count = rs.getInt(c.getString(R.string.countSQL))
                    id = rs.getInt(c.getString(R.string.idSQL))
                }
                rs.close()
                if (count == 1) {
                    //Get a value of clicked list item and escape the string to handle ' sign
                    val lyric = lv.getItemAtPosition(position).toString().replace("'", "''")

                    //Check whether user guesses the track to which the lyric part belongs
                    val rs1 = stmt.executeQuery(
                        c.getString(R.string.checkGuessSQL, id, lyric)
                    )
                    while (rs1.next()) {
                        val counter = rs1.getInt(c.getString(R.string.countSQL))
                        // If the track is found, count == 1
                        if (counter > 0) {
                            //If correct, delete from database and award user with points
                            val points = calculatePoints(id, gm, c)
                            deleteGuessed(id, parent.context, lv, gm, true)
                            setPoints(gm, true, points, x, c)
                        } else {
                            Toast.makeText(
                                parent.context, c.getString(R.string.tryAgainPrompt),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    rs1.close()

                } else {
                    Toast.makeText(
                        parent.context, c.getString(R.string.tryAgainPrompt),
                        Toast.LENGTH_SHORT
                    ).show()
                    val lyric = lv.getItemAtPosition(position).toString().replace("'", "''")
                    incrementAttempts(lyric, c)
                }
                stmt.close()
                conn.close()
            }
            b.setNegativeButton(c.getString(R.string.giveUpPrompt)) { _, _ ->
                val conn = establishConn(c)
                val stmt = conn.createStatement()
                val string = (lv.getItemAtPosition(position)).toString().replace("'", "''")
                var id = -1 //Error Value
                val rs = stmt.executeQuery(
                    c.getString(R.string.checkGuess1SQL, string)
                )
                while (rs.next()) {
                    id = rs.getInt(c.getString(R.string.songId))
                }
                rs.close()
                stmt.close()
                conn.close()
                deleteGuessed(id, parent.context, lv, gm, false)
                // Subtract points
                setPoints(gm, false, MAX_POINTS_TO_AWARD, x, c)
                stmt.close()
                conn.close()


            }
            // Disable the Give Up button when having not enough points to give up
            val dialog = b.show()
            if (getPoints(gm, c) < MAX_POINTS_TO_AWARD) {
                val but = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                but.isEnabled = false
            }
        }
    }

    private fun deleteGuessed(songId: Int, c: Context, lv: ListView, gm: Int, f: Boolean) {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        stmt.executeUpdate(c.getString(R.string.deleteFromBpSQL, songId))
        updateListView(c, getBackpack(conn, gm, c), lv)

        if (f) {
            Toast.makeText(c, c.getString(R.string.correct), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(c, c.getString(R.string.youGaveUpMessage), Toast.LENGTH_SHORT).show()
        }
        stmt.close()
        conn.close()
    }

    /**
     * Adds the collected lyric to the database. When done,
     * the backpack contents will be updated too
     */
    fun addToBackpack(m: Marker, gameMode: Int, c: Context) {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        val id = findSongIdByMarker(m, c)
        //Escaping the string to parse apostrophes to sql
        val lyricPart = m.title.replace("'", "''")
        stmt.executeUpdate(
            c.getString(R.string.bpInsertSQL, gameMode, id, lyricPart)
        )
        stmt.close()
        conn.close()
    }

    /**
     * Given the marker the function will return the unique id of the song which is stored in a DB
     */
    private fun findSongIdByMarker(m: Marker, c: Context): Int {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        //Getting the filename and handling the ' sign error in sql queries
        val lyricFile = m.tag.toString().replace("'", "''")
        val rs = stmt.executeQuery(c.getString(R.string.idByLyricSQL, lyricFile))
        var id = -4 // Error Value
        while (rs.next()) {
            id = rs.getInt(c.getString(R.string.idSQL))
        }
        rs.close()
        stmt.close()
        conn.close()
        return id
    }

    /**
     * Get the number of points in a specified game mode
     */
    fun getPoints(gm: Int, c: Context): Int {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        var points = -1 //Error Value
        val rs = stmt.executeQuery(c.getString(R.string.getPtsSQL, gm))
        while (rs.next()) {
            points = rs.getInt(c.getString(R.string.pointsSQL))
        }
        rs.clearWarnings()
        stmt.close()
        conn.close()
        return points
    }

    /**
     * Update the value of the points in a DB in a specified game mode. When isAddition = true
     * points will be added, otherwise - subtracted. Value is the number of points to be added
     * or subtracted
     */
    fun setPoints(gm: Int, isAddition: Boolean, value: Int, x: TextView, c: Context) {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        if (isAddition) {
            stmt.executeUpdate(
                c.getString(R.string.setPtsPlusSQL, value, gm)
            )
        } else {
            val oldPts = getPoints(gm, c)
            if (oldPts - value >= 0) {
                stmt.executeUpdate(
                    c.getString(R.string.setPtsMinusSQL, value, gm)
                )
            } else {
                stmt.executeUpdate(
                    c.getString(R.string.setPtsZroSQL, gm)
                )
            }
        }
        stmt.close()
        conn.close()
        x.text = c.getString(R.string.ptsCounter, getPoints(gm, c).toString())
    }

    /**
     * Get the number of attempts in guessing the song from different lyric parts
     * belonging to that song
     */
    private fun getAttempts(songId: Int, c: Context): Int {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        var attempts = -1 //Error Value
        val rs = stmt.executeQuery(
            c.getString(R.string.getAttemptsSQL, songId)
        )
        while (rs.next()) {
            attempts = rs.getInt(c.getString(R.string.sumAttemptsSQL))
        }
        rs.close()
        stmt.close()
        conn.close()
        return attempts
    }

    /**
     * Get the number of collected lyric parts of a specified song in a specified game mode
     */
    private fun numberOfLyricParts(gm: Int, songId: Int, c: Context): Int {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        var numLyrics = -1 //Error Value
        val rs = stmt.executeQuery(
            c.getString(R.string.numLyrPartsSQL, songId, gm)
        )

        while (rs.next()) {
            numLyrics = rs.getInt(c.getString(R.string.countSQL))
        }
        rs.close()
        stmt.close()
        conn.close()
        return numLyrics
    }

    /**
     * Increment the attempts counter in a database based on a lyric part which was clicked
     */
    private fun incrementAttempts(lp: String, c: Context) {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        stmt.executeUpdate(
            c.getString(R.string.updateAttemptsSQL, lp)
        )
        stmt.close()
        conn.close()
    }

    private fun calculatePoints(songId: Int, gm: Int, c: Context): Int {
        var points =
            (MAX_POINTS_TO_AWARD - (5 * getAttempts(songId, c) + 10 * numberOfLyricParts(
                gm,
                songId,
                c
            )))
        //No negative values should be encountered whatsoever
        if (points < 0) {
            points = 0
        }
        if (points > MAX_POINTS_TO_AWARD) {
            points = MAX_POINTS_TO_AWARD
        }
        if (points == MAX_POINTS_TO_AWARD - 10) {
            points = MAX_POINTS_TO_AWARD
        }
        return points
    }

    /**
     * Get the current size of the backpack
     */
    fun checkBpSize(gameMode: Int, c: Context): Int {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery(c.getString(R.string.checkBpSizeSQL, gameMode))
        var size = -1 //Error value
        while (rs.next()) {
            size = rs.getInt(c.getString(R.string.countSQL))
        }
        rs.close()
        stmt.close()
        conn.close()
        return size
    }

    fun getBpCapacity(gm: Int, c: Context): Int {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        var cap = -1 //Error value
        val rs = stmt.executeQuery(c.getString(R.string.checkBpCapacitySQL, gm))
        while (rs.next()) {
            cap = rs.getInt(c.getString(R.string.bpSizeSQL))
        }
        rs.close()
        stmt.close()
        conn.close()
        return cap
    }

    fun setBpCapacity(gm: Int, newCap: Int, c: Context) {
        val conn = establishConn(c)
        val stmt = conn.createStatement()
        stmt.executeUpdate(c.getString(R.string.setBpCapacity, newCap, gm))
        stmt.close()
        conn.close()
    }

}