package com.example.envilibrary

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class EnviLibraryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        val dexOutputDir: File = codeCacheDir
        File("/data/data/com.example.envilibrary/code_cache/.overlay/base.apk/classes3.dex").setReadOnly()
//        dexOutputDir.setReadOnly()
    }
}
