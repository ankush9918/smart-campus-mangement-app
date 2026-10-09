package com.vvit.smartcollege

import android.app.Application

import com.vvit.smartcollege.data.local.DatabaseProvider

class SmartCollegeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.init(this)
    }
}
