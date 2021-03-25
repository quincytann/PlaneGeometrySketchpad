package com.example.planegeometry.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class Sketchpad(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint: Paint
    private var bufferCanvas: Canvas? = null
    private var bufferBitmap: Bitmap? = null
    var path = Path()
    private var paintMode = 0
    private val rectLeftTop = Point()
    private val rectRightBottom = Point()
    private val circleCenter = Point()
    private var radius = 0f
    private var currencenter = 0f
    fun setPaintColor(color: Int) {
        paint.color = color
    }

    /**
     * StratThread:启动线程每0.3秒向onDraw刷新一次
     */
    private fun StratThread() {
        Thread {
            while (true) {
                postInvalidate()
                try {
                    Thread.sleep(300)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bufferBitmap = Bitmap.createBitmap(right, bottom, Bitmap.Config.ARGB_8888)
        bufferCanvas = Canvas(bufferBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bufferBitmap!!, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (paintMode) {
            PEN -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> path.moveTo(event.x, event.y)
                    MotionEvent.ACTION_MOVE -> {
                        path.lineTo(event.x, event.y)
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                    }
                }
                bufferCanvas!!.drawPath(path, paint)
            }
            RECT -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        rectLeftTop.x = event.x.toInt()
                        rectLeftTop.y = event.y.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        rectRightBottom.x = event.x.toInt()
                        rectRightBottom.y = event.y.toInt()
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                    }
                }
                bufferCanvas!!.drawColor(Color.WHITE)
                bufferCanvas!!.drawRect(rectLeftTop.x.toFloat(), rectLeftTop.y.toFloat(), rectRightBottom.x.toFloat(), rectRightBottom.y.toFloat(), paint)
            }
            CIRCLE -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        circleCenter.x = event.x.toInt()
                        circleCenter.y = event.y.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        radius = Math.sqrt(Math.pow((event.x - circleCenter.x).toDouble(), 2.0) + Math.pow((event.y - circleCenter.y).toDouble(), 2.0)).toFloat()
                        invalidate()
                    }
                    MotionEvent.ACTION_UP -> {
                    }
                }
                bufferCanvas!!.drawColor(Color.WHITE)
                bufferCanvas!!.drawCircle(circleCenter.x.toFloat(), circleCenter.y.toFloat(), radius, paint)
                when (event.action) {
                    MotionEvent.ACTION_UP ->                         //清除路径的内容
                        path.reset()
                }
            }
            ERASER -> when (event.action) {
                MotionEvent.ACTION_UP -> path.reset()
            }
        }
        return true
    }

    fun clear() {
        bufferCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    companion object {
        const val RECT = 1
        const val PEN = 0
        const val CIRCLE = 2
        const val ERASER = 3
    }

    init {
        paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.isDither = true
        StratThread()
    }
}