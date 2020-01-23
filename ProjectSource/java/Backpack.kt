package com.example.guessthelyrics

import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

private const val UPGRADE_PRICE = 500
private const val DEFAULT_CAPACITY = 10

class Backpack : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Allow networking on a UI thread
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backpack)
        val sesId = intent.extras!!.getInt(getString(R.string.bpGm))
        val pointView = findViewById<TextView>(R.id.textView5)

        pointView.text =
            getString(R.string.pointViewTxt, SqlAdapter().getPoints(sesId, applicationContext))
        val close = findViewById<Button>(R.id.collectBtn)
        close.setOnClickListener {
            this.finish()
        }

        val conn = SqlAdapter().establishConn(applicationContext)
        val bpContent = SqlAdapter().getBackpack(conn, sesId, applicationContext)
        val lv: ListView = findViewById(R.id.collectedLyricList)
        val bpUpgrade = findViewById<Button>(R.id.bpUpgrade)

        // Backpack might be only upgraded once to prevent inbalance
        if (SqlAdapter().getBpCapacity(sesId, applicationContext) != DEFAULT_CAPACITY) {
            bpUpgrade.isEnabled = false
            bpUpgrade.setBackgroundResource(R.drawable.bp_up_icon_bw)
        }

        bpUpgrade.setOnClickListener {
            val b = AlertDialog.Builder(this)
                .setTitle(getString(R.string.bpUp))
                .setMessage(getString(R.string.bpUpMess))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    // Double up the capacity of the backpack and subtract 500 points from the user
                    SqlAdapter().setBpCapacity(
                        sesId, SqlAdapter().getBpCapacity(sesId, applicationContext) * 2
                        , applicationContext
                    )
                    SqlAdapter().setPoints(
                        sesId,
                        false,
                        UPGRADE_PRICE,
                        pointView,
                        applicationContext
                    )
                    bpUpgrade.isEnabled = false
                    bpUpgrade.setBackgroundResource(R.drawable.bp_up_icon_bw)
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
            // Restrict upgrade if the number of points is insufficient
            val dialog = b.show()
            if (SqlAdapter().getPoints(sesId, applicationContext) < UPGRADE_PRICE) {
                val but = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                but.isEnabled = false
            }

        }
        SqlAdapter().updateListView(this.applicationContext, bpContent, lv)
        SqlAdapter().triggerGuess(lv, sesId, pointView, applicationContext)
    }
}
