package com.example.planegeometry

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.planegeometry.utils.ProxyClickListener
import com.example.planegeometry.views.MenuView.Companion.CIRCLE
import com.example.planegeometry.views.MenuView.Companion.CLEAR
import com.example.planegeometry.views.MenuView.Companion.ERASER
import com.example.planegeometry.views.MenuView.Companion.PEN
import com.example.planegeometry.views.MenuView.Companion.RECTANGULAR
import com.example.planegeometry.views.MenuView.Companion.SEGMENT
import com.example.planegeometry.views.MenuView.Companion.TRIANGLE
import kotlinx.android.synthetic.main.draw_layout.*
import java.lang.Exception


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

        // 添加侧边点击选项后的回调
        val clickListener = mutableListOf<() -> Unit>().apply {
            add {
                board_view.setPaintMode(PEN)
                startHideMenuBar()
            }
            add {
                board_view.apply {
                    clearDraw()
                }
            }
            add {
                board_view.setPaintMode(ERASER)
                startHideMenuBar()
            }
            add {
                board_view.revoked()
            }
            add {
                board_view.unRevoked()
            }
            add {
                board_view.setPaintMode(SEGMENT)
                startHideMenuBar()
            }
            add {
                board_view.setPaintMode(TRIANGLE)
                startHideMenuBar()
            }
            add {
                board_view.setPaintMode(RECTANGULAR)
                startHideMenuBar()
            }
            add {
                board_view.setPaintMode(CIRCLE)
                startHideMenuBar()
            }
        }
        menu_page.setClickItemCallBack(clickListener)
    }

    private fun startHideMenuBar() {
        Thread {
            try {
                Thread.sleep(500)
            } catch (e: Exception) {

            }
            drawer_layout.closeDrawer(GravityCompat.END)
        }.start()
    }
}