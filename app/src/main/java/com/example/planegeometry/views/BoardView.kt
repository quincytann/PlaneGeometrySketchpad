package com.example.planegeometry.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.utils.DimenUtils
import com.example.planegeometry.views.BoardView.Companion.TYPE_LINE
import com.example.planegeometry.views.MenuView.Companion.CIRCLE
import com.example.planegeometry.views.MenuView.Companion.CLEAR
import com.example.planegeometry.views.MenuView.Companion.ERASER
import com.example.planegeometry.views.MenuView.Companion.PEN
import com.example.planegeometry.views.MenuView.Companion.RECTANGULAR
import com.example.planegeometry.views.MenuView.Companion.SEGMENT
import com.example.planegeometry.views.MenuView.Companion.TRIANGLE
import kotlin.math.sqrt

class BoardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 路径
    private var path: Path = Path()
    private var prePath: Path = Path()

    // 画笔
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var drawPaint: Paint
    private var eraserPaint: Paint
    private var textPaint: Paint
    private var paintMode: Int = PEN

    // 画布 && bitmap
    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap

    // 一些临时变量
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
            strokeWidth = DimenUtils.dp2px(3f)
            isAntiAlias = true //开启抗锯齿
            isDither = true //开启防抖
        }

        drawPaint = Paint(paint)

        eraserPaint = Paint(paint)
        eraserPaint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            strokeWidth = DimenUtils.dp2px(8f)
        }

        textPaint = Paint(paint)
        textPaint.apply {
            color = Color.BLUE
            style = Paint.Style.FILL
            textSize = DimenUtils.sp2px(12f)
        }

        isDrawingCacheEnabled = true
    }

    fun setPaintMode(mode: Int) {
        paintMode = mode
        paint = if (mode == ERASER) eraserPaint else drawPaint
        path.reset()
        clickTimes = 0
    }

    fun clearDraw() {
        path.reset()
        mPaintedList.clear()
        mRevokedList.clear()
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        clickTimes = 0
        pointCount = 0
        invalidate()
    }

    fun canRevoked(): Boolean = mPaintedList.size > 0

    fun revoked() {
        reDraw(mPaintedList)
    }

    fun canUnRevoked(): Boolean = mRevokedList.size > 0

    fun unRevoked() {
        reDraw(mRevokedList)
    }

    fun getBitmap(): Bitmap {
        val bm = drawingCache
        val result = Bitmap.createBitmap(bm)
        destroyDrawingCache()
        return result
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!this::bitmap.isInitialized) {
            bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        CLog.d(TAG, "onTouchEvent")
        when (paintMode) {
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
                        mPaintedList.add(PaintData(Paint(paint), Path(path)))
                        path.reset()
                    }
                }
            }

            SEGMENT -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTimes++
                        drawPointText(x, y)
                        if (clickTimes == 1) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            canvas.drawPath(path, paint)
                            mPaintedList.add(PaintData(Paint(paint), Path(path)))
                            path.reset()
                            clickTimes = 0
                        }
                    }
                }
            }

            TRIANGLE -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTimes++
                        drawPointText(x, y)
                        when (clickTimes) {
                            1 -> {
                                path.moveTo(x, y)
                                preX = x
                                preY = y
                            }
                            2 -> {
                                path.lineTo(x, y)
                                drawPath()
                            }
                            3 -> {
                                path.lineTo(x, y)
                                drawPath()
                                path.lineTo(preX, preY)
                                drawPath()

                                clickTimes = 0
                                path.reset()
                            }
                        }
                    }
                }
            }

            RECTANGULAR -> {
                // 暂且使用两点式画出来，实时拖动绘制还有点问题没找到合适解决方案
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTimes++
                        if (clickTimes == 1) {
                            drawPointText(x, y)
                            preX = x
                            preY = y
                        } else if (clickTimes == 2) {
                            drawRectFRemainingPointsText(preX, preY, x, y)
                            val rectF = getRectF(preX, preY, x, y)
                            path.addRect(rectF, Path.Direction.CW)
                            drawPath()
                            path.reset()
                            clickTimes = 0
                        }
                    }
                }
            }

            CIRCLE -> {
                // 暂且使用两点式画出来，实时拖动绘制还有点问题没找到合适解决方案
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTimes++
                        if (clickTimes == 1) {
                            drawPointText(x, y)
                            preX = x
                            preY = y
                        } else if (clickTimes == 2) {
                            val radius = sqrt((x - preX) * (x - preX) + (y - preY) * (y - preY))
                            path.addCircle(preX, preY, radius, Path.Direction.CW)
                            drawPath()
                            path.reset()
                            clickTimes = 0
                        }
                    }
                }
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }


    private fun reDraw(paintList: MutableList<PaintData>) {
        val lastPaint = paintList.removeLast()
        if (paintList === mPaintedList) {
            mRevokedList.add(lastPaint)
            // 将一系列关联的点一起带上 否则会显得不美观
            while (paintList.isNotEmpty() && paintList.last().mType == TYPE_POINT) {
                mRevokedList.add(paintList.removeLast())
            }
        } else {
            mPaintedList.add(lastPaint)
//            while (paintList.isNotEmpty() && paintList.last().mType != TYPE_POINT) {
//                mPaintedList.add(paintList.removeLast())
//            }
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        for (paintData in mPaintedList) {
            if (paintData.mType == TYPE_LINE) {
                paintData.drawPath(canvas)
            } else if (paintData.mType == TYPE_POINT) {
                paintData.drawPointText(canvas)
            }
        }
        invalidate()
    }

    private fun getPointText(): String = "P${pointCount}"

    // 写出点的文本形式并记录
    private fun drawPointText(x: Float, y: Float) {
        canvas.apply {
            drawPoint(x, y, paint)
            drawText(getPointText(), x, y, textPaint)
        }
        mPaintedList.add(PaintData(Paint(textPaint), null, PointData(x, y, getPointText()), TYPE_POINT))
        pointCount++
    }

    // 画出path并记录
    private fun drawPath() {
        canvas.drawPath(path, paint)
        mPaintedList.add(PaintData(Paint(paint), Path(path)))
    }

    // 除了第一个点击的点剩余的三个点
    private fun drawRectFRemainingPointsText(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawPointText(x1, y2)
        drawPointText(x2, y1)
        drawPointText(x2, y2)
    }

    // 根据两次点击点的坐标构造矩形
    private fun getRectF(x1: Float, y1: Float, x2: Float, y2: Float): RectF{
        if (x1 <= x2 && y1 >= y2) return RectF(x1, y2, x2, y1)
        if (x1 <= x2 && y1 <= y2) return RectF(x1, y1, x2, y2)
        if (x1 >= x2 && y1 >= y2) return RectF(x2, y2, x1, y1)
        if (x1 >= x2 && y1 <= y2) return RectF(x2, y1, x1, y2)
        return RectF()
    }

    companion object {
        const val TAG = "BoardView"
        const val TYPE_POINT = 1
        const val TYPE_LINE = 2
    }
}


data class PaintData(
        val mPaint: Paint,
        val mPath: Path? = null,
        val mPointText: PointData? = null,
        val mType: Int = TYPE_LINE
) {

    fun drawPath(canvas: Canvas) {
        canvas.drawPath(mPath!!, mPaint)
    }

    fun drawPointText(canvas: Canvas) {
        canvas.drawText(mPointText!!.text, mPointText.x, mPointText.y, mPaint)
    }
}

data class PointData(val x: Float, val y: Float, val text: String)