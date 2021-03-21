package com.example.planegeometry

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sketchpad = findViewById<Sketchpad>(R.id.myView)
        /**
         * 红色
         */
        findViewById<Button>(R.id.btn1).setOnClickListener(View.OnClickListener {
            sketchpad.setPaintColor(
                    Color.RED
            )
        })
        /**
         * 蓝色
         */
        findViewById<Button>(R.id.btn2).setOnClickListener(View.OnClickListener {
            sketchpad.setPaintColor(
                    Color.BLUE
            )
        })
        /**
         * 矩形
         */
        findViewById<Button>(R.id.btn3).setOnClickListener(View.OnClickListener {
            sketchpad.setPaintMode(
                    Sketchpad.RECT
            )
        })
        /**
         * 圆形
         */
        findViewById<Button>(R.id.btn4).setOnClickListener(View.OnClickListener {
            sketchpad.setPaintMode(
                    Sketchpad.CIRCLE
            )
        })
        /**
         * 画笔
         */
        findViewById<Button>(R.id.btn5).setOnClickListener(View.OnClickListener {
            sketchpad.setPaintMode(
                    Sketchpad.PEN
            )
        })
        /**
         * 清除
         */
        findViewById<Button>(R.id.btn6).setOnClickListener(View.OnClickListener { sketchpad.clear() })
    }
}