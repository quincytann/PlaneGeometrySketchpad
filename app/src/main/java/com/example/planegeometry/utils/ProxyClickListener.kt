package com.example.planegeometry.utils

import android.view.View

/**
 * 防抖动监听事件
 */

class ProxyClickListener(private val action: (() -> Unit)?) : View.OnClickListener {

    private var lastClickTime: Long = 0
    private var intervalsTime: Long = 100

    override fun onClick(v: View?) {
        if(System.currentTimeMillis() - lastClickTime >= intervalsTime) {
            action?.invoke()
            lastClickTime = System.currentTimeMillis()
        }
    }

}