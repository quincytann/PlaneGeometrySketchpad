package com.example.planegeometry.utils

object DimenUtil {

    private val scale: Float = MyApplication.context.resources.displayMetrics.density
    private val fontScale: Float = MyApplication.context.resources.displayMetrics.scaledDensity

    fun dip2px(dp: Float): Float {
        return dp * scale + 0.5f
    }

    fun px2dip(px: Float): Float {
        return px / scale + 0.5f
    }

    fun sp2px(spValue: Float): Float {
        return spValue * fontScale + 0.5f
    }

    fun px2sp(pxValue: Float): Float {
        return pxValue / fontScale + 0.5f
    }
}