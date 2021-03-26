package com.example.planegeometry.utils

import android.content.res.Resources
import android.util.TypedValue

object DimenUtils {
    private val sResource = Resources.getSystem()
    fun dp2px(dp: Float): Float {
        val dm = sResource.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm)
    }

    fun dp2pxInt(dp: Float): Int {
        return dp2px(dp).toInt()
    }

    fun sp2px(sp: Float): Float {
        val dm = sResource.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, dm)
    }

    fun sp2pxInt(sp: Float): Int {
        return sp2px(sp).toInt()
    }
}