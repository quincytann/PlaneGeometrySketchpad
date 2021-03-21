package com.example.planegeometry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.utils.ScreenUtil
import kotlinx.android.synthetic.main.draw_layout.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.draw_layout)
        setSupportActionBar(toolbar)

        initView()
        initAction()
    }

    private fun initView() {
        //CLog.d(TAG, "width: ${ScreenUtil.getScreenWidth()}  height: ${ScreenUtil.getScreenHeight()}")

    }

    private fun initAction() {
        // todo 点击事件增加防抖机制
        menu.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.END)
        }
    }
}