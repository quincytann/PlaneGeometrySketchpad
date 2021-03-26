package com.example.planegeometry.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.utils.DimenUtils
import com.example.planegeometry.views.MenuView.Companion.CLEAR
import com.example.planegeometry.views.MenuView.Companion.ERASER
import com.example.planegeometry.views.MenuView.Companion.PEN
import com.example.planegeometry.views.MenuView.Companion.RECTANGULAR
import com.example.planegeometry.views.MenuView.Companion.SEGMENT
import com.example.planegeometry.views.MenuView.Companion.TRIANGLE

class BoardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var path: Path = Path()
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var textPaint: Paint
    private var paintMode: Int = PEN
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap
    private lateinit var holdCanvas: Canvas
    private lateinit var holdBitmap: Bitmap

    private var preX = 0f
    private var preY = 0f
    private var clickTimes: Int = 0
    private var pointCount: Int = 0

    private var mPaintedList: MutableList<PaintData> = ArrayList()
    private var mRevokedList: MutableList<PaintData> = ArrayList()

    init {
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = DimenUtils.dp2px(2f)
            isAntiAlias = true //开启抗锯齿
            isDither = true //开启防抖
        }
        textPaint = Paint(paint)
        textPaint.apply {
            color = Color.BLUE
            style = Paint.Style.FILL
            textSize = DimenUtils.sp2px(15f)
        }
    }

    fun setPaintMode(mode: Int) {
        paintMode = mode
        if (mode == ERASER) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.strokeWidth = DimenUtils.dp2px(8f)
        } else {
            paint.xfermode = null
            paint.strokeWidth = DimenUtils.dp2px(2f)
        }
        path.reset()
        clickTimes = 0
    }

    fun clearDraw() {
        path.reset()
        mPaintedList.clear()
        mRevokedList.clear()
        holdCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        clickTimes = 0
        pointCount = 0
        invalidate()
    }

    fun revoked() {
        reDraw(mPaintedList)
    }

    fun unRevoked() {
        reDraw(mRevokedList)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!this::bitmap.isInitialized) {
            bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
        }
        if (!this::holdBitmap.isInitialized) {
            holdBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            holdCanvas = Canvas(holdBitmap)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        CLog.d(TAG, "onTouchEvent")
        when(paintMode) {
            PEN, ERASER -> {
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
                        canvas.drawPath(path, paint)
                    }
                    MotionEvent.ACTION_UP -> {
                        mPaintedList.add(PaintData(Paint(paint), Path(path)))    // 记录每一笔
                        path.reset()
                    }
                }
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

        // 大于最大可缓存的笔画固化下来
        while (mPaintedList.size > MAX_PAINT_RECORD) {
            val paintData = mPaintedList.removeFirst()
            paintData.draw(holdCanvas)
        }
        canvas.drawBitmap(holdBitmap, 0f, 0f, null)
        for (paint in mPaintedList) {
            paint.draw(canvas)
        }

        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }


    private fun reDraw(paintList: MutableList<PaintData>) {
        if (paintList.size > 0) {
            val paint = paintList.removeLast()
            if (paintList === mPaintedList) {
                mRevokedList.add(paint)
            } else {
                mPaintedList.add(paint)
            }
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            invalidate()
        }
    }


    companion object {
        const val TAG = "BoardView"
        const val MAX_PAINT_RECORD = 20
    }
}



data class PaintData(var mPaint: Paint, var mPath: Path) {

    fun draw(canvas: Canvas) {
        canvas.drawPath(mPath, mPaint)
    }
}