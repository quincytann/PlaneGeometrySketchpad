package com.example.planegeometry.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.views.MenuView.Companion.CLEAR
import com.example.planegeometry.views.MenuView.Companion.PEN
import com.example.planegeometry.views.MenuView.Companion.RECTANGULAR
import com.example.planegeometry.views.MenuView.Companion.SEGMENT
import com.example.planegeometry.views.MenuView.Companion.TRIANGLE

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    //路径
    private var path: Path

    //画笔
    private var paint: Paint
    private var textPaint: Paint
    private var paintMode: Int = 0

    //之前的坐标
    private var preX = 0f
    private var preY = 0f
    private lateinit var bitmap: Bitmap
    private var canvas: Canvas

    private var clickTimes: Int = 0
    private var pointCount: Int = 0

    init {
        CLog.d(TAG, "init")
        canvas = Canvas()
        path = Path()
        paint = Paint(Paint.DITHER_FLAG)
        // 初始化画笔
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true //开启抗锯齿
            isDither = true //开启防抖
        }
        textPaint = Paint(paint)
        textPaint.apply {
            color = Color.BLUE
            style = Paint.Style.FILL
            strokeWidth = 1f
            textSize = 30f
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        CLog.d(TAG, "onLayout")
        super.onLayout(changed, left, top, right, bottom)
        if (!this::bitmap.isInitialized) {
            bitmap = Bitmap.createBitmap(right, bottom, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        CLog.d(TAG, "onTouchEvent")
        when(paintMode) {
            PEN -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        path.moveTo(x, y)
                        preX = x
                        preY = y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        path.quadTo(preX, preY, x, y)
                        preX = x
                        preY = y
                        invalidate()
                    }
                }
                canvas.drawPath(path, paint)
            }
            CLEAR -> {

            }
            SEGMENT -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pointCount ++
                        clickTimes ++
                        preX = x
                        preY = y
                        if (clickTimes == 2) {
                            clickTimes = 0
                            path.lineTo(x, y)
                        } else {
                            path.moveTo(x, y)
                        }
                    }
                }
                canvas.apply {
                    drawPoint(x, y, paint)
                    drawText("P${pointCount}", preX, preY, textPaint)
                    drawPath(path, paint)
                }
                invalidate()
            }
            TRIANGLE -> {

            }
            RECTANGULAR -> {

            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }

    fun setPaintMode(mode: Int) {
        paintMode = mode
        clickTimes = 0
    }

    fun clearDraw() {
        path.reset()
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        clickTimes = 0
        pointCount = 0
        invalidate()
    }

    companion object {
        const val TAG = "BoardView"
    }
}
