package com.example.planegeometry.utils

import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

object FileUtil {

    fun test() {
        CLog.d("quincy", Environment.getExternalStorageDirectory().toString())
        CLog.d("quincy", Environment.getDataDirectory().toString())
        CLog.d("quincy", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString())
    }

    fun saveImg(bm: Bitmap): Boolean {
        // todo 格式区别 && bitmap来源 && 保存步骤方式
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "IMG-" + System.currentTimeMillis() + ".png")
        if (file.exists()) {
            file.delete()
        }
        try {
            val fos = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

}