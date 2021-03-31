package com.example.planegeometry.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.planegeometry.R
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.utils.DimenUtils
import com.example.planegeometry.utils.MyApplication
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
            strokeWidth = DimenUtils.dp2px(2f)
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
            textSize = DimenUtils.sp2px(15f)
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

    fun getBitmap(): Bitmap = bitmap


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
                        mPaintedList.add(PaintData(Paint(paint), Path(path)))    // 抬起手指后记录每一笔，方便撤销操作
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
                        if (clickTimes == 1) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                        canvas.apply {
                            drawPoint(x, y, paint)
                            drawTextOnPath("P${pointCount}",path, x, y, textPaint)
                            drawPath(path, paint)
                            mPaintedList.add(PaintData(Paint(paint), Path(path)))
                        }
                        if (clickTimes == 2) {
                            path.reset()
                            clickTimes = 0
                        }
                    }
                }
            }

            TRIANGLE -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTimes ++
                        pointCount ++
                        canvas.apply {
                            drawPoint(x, y, paint)
                            drawText("P${pointCount}", x, y, textPaint)
                        }
                        when (clickTimes) {
                            1 -> {
                                path.moveTo(x, y)
                                preX = x
                                preY = y
                            }
                            2 -> {
                                path.lineTo(x, y)
                            }
                            3 -> {
                                path.lineTo(x, y)
                                path.lineTo(preX, preY)
                                mPaintedList.add(PaintData(Paint(paint), Path(path)))
                                canvas.drawPath(path, paint)

                                clickTimes = 0
                                path.reset()
                            }
                        }
                    }
                }
            }

            RECTANGULAR -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickTimes ++
                        if (clickTimes == 1) {
                            canvas.apply {
                                drawPoint(x, y, paint)
                            }
                            preX = x
                            preY = y
                        } else if (clickTimes == 2) {
                            path.addRect(preX, preY, x, y, Path.Direction.CW)
                            canvas.drawPath(path, paint)
                            mPaintedList.add(PaintData(Paint(paint), Path(path)))
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
                        clickTimes ++
                        if (clickTimes == 1) {
                            pointCount ++
                            canvas.apply {
                                drawPoint(x, y, paint)
                                drawText("P${pointCount}", x, y, textPaint)
                            }
                            preX = x
                            preY = y
                        } else if (clickTimes == 2) {
                            val radius = sqrt((x-preX)*(x-preX)+(y-preY)*(y-preY))
                            path.addCircle(preX, preY, radius, Path.Direction.CW)
                            canvas.drawPath(path, paint)
                            mPaintedList.add(PaintData(Paint(paint), Path(path)))
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
        } else {
            mPaintedList.add(lastPaint)
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        for (paintData in mPaintedList) {
            paintData.draw(canvas)
        }
        invalidate()
    }


    companion object {
        const val TAG = "BoardView"
    }
}



data class PaintData(var mPaint: Paint, var mPath: Path) {

    fun draw(canvas: Canvas) {
        canvas.drawPath(mPath, mPaint)
    }
}