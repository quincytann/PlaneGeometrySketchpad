package com.example.planegeometry

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.planegeometry.colorpicker.ColorPickerDialog
import com.example.planegeometry.colorpicker.OnColorPickerListener
import com.example.planegeometry.coordinateaxischart.FuncUtils.getFunctionLineByInputData
import com.example.planegeometry.funtionInput.FunctionInputData
import com.example.planegeometry.funtionInput.FunctionInputDialog
import com.example.planegeometry.funtionInput.OnFunctionInputListener
import com.example.planegeometry.utils.FileUtil
import com.example.planegeometry.utils.ProxyClickListener
import com.example.planegeometry.views.MenuView.Companion.CIRCLE
import com.example.planegeometry.views.MenuView.Companion.ERASER
import com.example.planegeometry.views.MenuView.Companion.MOVE
import com.example.planegeometry.views.MenuView.Companion.PEN
import com.example.planegeometry.views.MenuView.Companion.POINT
import com.example.planegeometry.views.MenuView.Companion.RECTANGULAR
import com.example.planegeometry.views.MenuView.Companion.SEGMENT
import com.example.planegeometry.views.MenuView.Companion.TRIANGLE
import kotlinx.android.synthetic.main.main_activity__layout.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity__layout)
        setSupportActionBar(toolbar)

        initView()
        initAction()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSaveImg()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    R.string.toast_no_file_read_and_write_permission,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initView() {
        // 去除侧边栏滑出时底部页面默认阴影
        drawer_layout.setScrimColor(Color.TRANSPARENT)

        // 隐藏statusBar&&navigationBar得到全屏效果
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility != uiOptions) {
                decorView.systemUiVisibility = uiOptions
            }
        }
//        val controller = ViewCompat.getWindowInsetsController(decorView)
//        controller?.hide(WindowInsetsCompat.Type.systemBars())
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
                showColorPicker()
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
                    Toast.makeText(
                        this@MainActivity,
                        R.string.toast_no_more_record,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            add {
                if (board_view.canUnRevoked()) {
                    board_view.unRevoked()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.toast_no_more_record,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            add {
                board_view.drawOrHideAxis()
            }
            add {
                board_view.shouldShowAxis()
                showFunctionInput()
            }
            add {
                board_view.setPaintMode(POINT)
                startHideMenuBar()
            }
            add {
                board_view.setPaintMode(MOVE)
                startHideMenuBar()
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
                    Toast.makeText(
                        this@MainActivity,
                        R.string.toast_no_file_read_and_write_permission,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            add {
                val state = verifyStoragePermission()
                if (state) {
                    showShare()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.toast_no_file_read_and_write_permission,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        menu_page.setClickItemCallBack(clickListener)
    }

    private fun showFunctionInput() {
        FunctionInputDialog(this@MainActivity, object : OnFunctionInputListener{
            override fun onFunctionParametersFixed(data: FunctionInputData?) {
                data?.let {
                    val line = getFunctionLineByInputData(it)
                    board_view.addFunctionLine(line)
                } ?: Toast.makeText(
                    this@MainActivity,
                    R.string.toast_parameter_is_not_complete,
                    Toast.LENGTH_SHORT
                ).show()
            }

        }).show()
    }

    private fun showColorPicker() {
        ColorPickerDialog(
            this@MainActivity,
            board_view.getPaintColor(),
            object : OnColorPickerListener {
                override fun onColorCancel(dialog: ColorPickerDialog?) {

                }

                override fun onColorChange(dialog: ColorPickerDialog?, color: Int) {

                }

                override fun onColorConfirm(dialog: ColorPickerDialog?, color: Int) {
                    board_view.setPaintColor(color)
                }
            }
        ).show()
    }

    private fun showShare() {
        val file = FileUtil.getShareFile(board_view.getBitmap())
        val uri = FileProvider.getUriForFile(
            this@MainActivity,
            "com.example.planegeometry.provider",
            file
        )
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(this, null))
        }
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
            Toast.makeText(this@MainActivity, R.string.toast_save_success, Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this@MainActivity, R.string.toast_save_fail, Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyStoragePermission(): Boolean {
        try {
            val permission = ActivityCompat.checkSelfPermission(
                this,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )
            return if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE_CODE
                )
                false
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    companion object {
        const val TAG = "MainActivity"
        const val REQUEST_EXTERNAL_STORAGE_CODE = 1
        val PERMISSIONS_STORAGE = arrayOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    }

}