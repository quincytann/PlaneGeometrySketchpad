package com.example.planegeometry.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.planegeometry.utils.CLog

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    //路径
    private var path: Path? = null

    //画笔
    var paint: Paint? = null
    private var paintMode: Int = 0

    //之前的坐标
    private var preX = 0f
    private var preY = 0f
    var bitmap: Bitmap? = null
    var canvas: Canvas? = null

    init {
        CLog.d(TAG, "init")
        canvas = Canvas()
        path = Path()
        paint = Paint(Paint.DITHER_FLAG)
        // 初始化画笔
        paint!!.apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true //开启抗锯齿
            isDither = true //开启防抖
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        CLog.d(TAG, "onLayout")
        super.onLayout(changed, left, top, right, bottom)
        bitmap = Bitmap.createBitmap(right, bottom, Bitmap.Config.ARGB_8888)
        canvas!!.setBitmap(bitmap)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path!!.moveTo(x, y)
                preX = x
                preY = y
            }
            MotionEvent.ACTION_MOVE -> {
                path!!.quadTo(preX, preY, x, y)
                preX = x
                preY = y
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        CLog.d(TAG, "onDraw")
        super.onDraw(canvas)
        //val bmpPaint = Paint()
        //canvas.drawBitmap(bitmap!!, 0f, 0f, bmpPaint)
        canvas.drawPath(path!!, paint!!)
    }

    fun setPaintMode(mode: Int) {
        paintMode = mode
    }

    fun clearDraw() {
        path!!.reset()
        //canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate()
    }

    companion object {
        const val TAG = "BoardView"
    }
}
