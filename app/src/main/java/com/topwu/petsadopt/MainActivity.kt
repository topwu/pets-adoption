package com.topwu.petsadopt

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        if (mapFragment.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }
}
