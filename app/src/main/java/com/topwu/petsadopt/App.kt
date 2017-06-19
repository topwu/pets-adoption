package com.topwu.petsadopt

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Fresco.initialize(this)
    }
}