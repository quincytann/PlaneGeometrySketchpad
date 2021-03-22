package com.example.planegeometry

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.planegeometry.utils.ProxyClickListener
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
        drawer_layout.apply {
            setScrimColor(Color.TRANSPARENT) // 去除侧边栏滑出时底部页面默认阴影
        }

        // 隐藏statusBar&&navigationBar得到全屏效果
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        decorView.systemUiVisibility = uiOptions
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility != uiOptions) {
                decorView.systemUiVisibility = uiOptions
            }
        }
    }

    private fun initAction() {
        menu.setOnClickListener(ProxyClickListener {
            drawer_layout.openDrawer(GravityCompat.END)
        })

    }
}