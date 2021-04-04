package com.example.planegeometry.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


object FileUtil {

    fun saveImg(bm: Bitmap): Boolean {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "IMG-" + System.currentTimeMillis() + ".jpg"
        )
        if (file.exists()) {
            file.delete()
        }
        try {
            val fos = FileOutputStream(file)
            val state = bm.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()

            // 通知更新到相册
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
            MyApplication.context.sendBroadcast(intent)

            return state
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    // 确保有读写权限的前提下保存分享图片
    fun getShareFile(bm: Bitmap): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "IMG-" + System.currentTimeMillis() + ".jpg"
        )
        if (file.exists()) {
            file.delete()
        }
        val fos = FileOutputStream(file)
        bm.compress(Bitmap.CompressFormat.JPEG, 80, fos)
        fos.flush()
        fos.close()
        return file
    }

}