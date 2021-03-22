package com.example.planegeometry.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.LinearLayout
import com.example.planegeometry.R
import com.example.planegeometry.utils.MyApplication

class MenuView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(MyApplication.context).inflate(R.layout.menu_view_layout, this, true)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

}