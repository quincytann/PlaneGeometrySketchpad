package com.example.planegeometry

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.example.planegeometry.utils.FileUtil
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.draw_layout)
        setSupportActionBar(toolbar)

        initView()
        initAction()
    }

    private fun initView() {
        //CLog.d(TAG, "width: ${ScreenUtil.getScreenWidth()}  height: ${ScreenUtil.getScreenHeight()}")

        // 去除侧边栏滑出时底部页面默认阴影
        drawer_layout.setScrimColor(Color.TRANSPARENT)

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
                if (board_view.canRevoked()) {
                    board_view.revoked()
                } else {
                    Toast.makeText(this@MainActivity, R.string.toast_no_more_record, Toast.LENGTH_SHORT).show()
                }
            }
            add {
                if (board_view.canUnRevoked()) {
                    board_view.unRevoked()
                } else {
                    Toast.makeText(this@MainActivity, R.string.toast_no_more_record, Toast.LENGTH_SHORT).show()
                }
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
            add {
                val state = verifyStoragePermission()
                if (state) {
                    startSaveImg()
                } else {
                    Toast.makeText(this@MainActivity, R.string.toast_no_file_write_permission, Toast.LENGTH_SHORT).show()
                }
            }
            add {
                // todo share
            }
        }
        menu_page.setClickItemCallBack(clickListener)
    }

    private fun startHideMenuBar() {
        Thread {
            try {
                Thread.sleep(500)
                drawer_layout.closeDrawer(GravityCompat.END)
            } catch (e: Exception) {
                // 暂不处理
            }
        }.start()
    }

    private fun startSaveImg() {
        val saveState = FileUtil.saveImg(board_view.getBitmap())
        if (saveState) {
            Toast.makeText(this@MainActivity, R.string.toast_save_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, R.string.toast_save_fail, Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyStoragePermission(): Boolean {
        try {
            val permission = ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE")
            return if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE_CODE)
                false
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSaveImg()
            } else {
                Toast.makeText(this@MainActivity, R.string.toast_no_file_write_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_EXTERNAL_STORAGE_CODE = 1
        val PERMISSIONS_STORAGE = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
    }
}