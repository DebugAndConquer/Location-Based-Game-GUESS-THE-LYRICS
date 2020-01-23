package com.example.guessthelyrics

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val openClassic = findViewById<Button>(R.id.classicButton)
        openClassic.setOnClickListener {
            val intent = Intent(this, GameWindow::class.java)
            intent.putExtra(getString(R.string.gmExtra), 0)
            startActivity(intent)
        }
        val openCurrent = findViewById<Button>(R.id.currentButton)
        openCurrent.setOnClickListener {
            val intent = Intent(this, GameWindow::class.java)
            intent.putExtra(getString(R.string.gmExtra), 1)
            startActivity(intent)
        }
    }
}
