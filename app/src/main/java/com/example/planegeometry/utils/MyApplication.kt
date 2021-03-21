package com.example.planegeometry.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 获取全局context工具类
 */

class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}