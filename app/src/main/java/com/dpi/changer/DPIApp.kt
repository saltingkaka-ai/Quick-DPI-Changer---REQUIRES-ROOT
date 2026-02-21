package com.dpi.changer

import android.app.Application
import com.dpi.changer.data.local.PresetDataStore

class DPIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PresetDataStore.initialize(this)
    }
}