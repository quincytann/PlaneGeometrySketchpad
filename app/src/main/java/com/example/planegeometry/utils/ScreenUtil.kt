package com.example.planegeometry.utils

import android.content.Context
import android.view.WindowManager

/**
 * 屏幕相关工具类
 */

object ScreenUtil {
    //获取屏幕的宽度
    fun getScreenWidth(): Int {
        val manager = MyApplication.context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        return display.width
    }

    //获取屏幕的高度
    fun getScreenHeight(): Int {
        val manager = MyApplication.context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        return display.height
    }
}