package com.example.planegeometry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.draw_layout.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.draw_layout)
        setSupportActionBar(toolbar)

        initAction()
    }


    private fun initAction() {
        // todo 点击事件增加防抖机制
        menu.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.END)
        }
    }
}