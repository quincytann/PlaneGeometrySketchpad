package com.example.planegeometry

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class BoardView : View {
    //路径
    private var path: Path? = null

    //画笔
    var paint: Paint? = null

    //之前的坐标
    private var preX = 0f
    private var preY = 0f
    var bitmap: Bitmap? = null
    var canvas: Canvas? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        bitmap = Bitmap.createBitmap(VIEW_WIDTH, VIEW_HEIGHT,
                Bitmap.Config.ARGB_8888)
        canvas = Canvas()
        path = Path()
        canvas!!.setBitmap(bitmap)
        paint = Paint(Paint.DITHER_FLAG)
        paint!!.color = Color.RED
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = 10f
        paint!!.isAntiAlias = true
        paint!!.isDither = true
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
        super.onDraw(canvas)
        val bmpPaint = Paint()
        canvas.drawBitmap(bitmap!!, 0f, 0f, bmpPaint)
        canvas.drawPath(path!!, paint!!)
    }

    companion object {
        //默认画布大小
        var VIEW_WIDTH = 500
        var VIEW_HEIGHT = 600
    }
}
