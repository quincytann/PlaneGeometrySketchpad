package com.example.planegeometry.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.planegeometry.coordinateaxischart.*
import com.example.planegeometry.coordinateaxischart.exception.FunctionNotValidException
import com.example.planegeometry.coordinateaxischart.exception.FunctionTypeException
import com.example.planegeometry.coordinateaxischart.type.*
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.utils.DimenUtil
import com.example.planegeometry.views.BoardView.Companion.DRAW_TYPE_LINE
import com.example.planegeometry.views.MenuView.Companion.CIRCLE
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

    // 画笔
    private var paint: Paint = Paint()
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

    // 坐标轴相关
    private var showAxis = false
    private var width = 0f
    private var height = 0f
    private var axisPaint: Paint = Paint()
    private var functionLinePaint: Paint = Paint()
    private var pointPaint: Paint = Paint()
    private val lineColor = Color.RED
    private var axisColor = DEFAULT_AXIS_COLOR
    private var axisWidth = DEFAULT_AXIS_WIDTH
    private val functionLineWidth = DEFAULT_FUNCTION_LINE_WIDTH
    private var axisPointRadius = DEFAULT_AXIS_POINT_RADIUS
    private var segmentSize = DEFAULT_SEGMENT_SIZE
    private var dx = DEFAULT_PRECISION
    private val coordinateTextSize = DEFAULT_COORDINATE_TEXT_SIZE
    private var max = DEFAULT_MAX
    private var unitLength = 0f
    private var xMax = 0
    private var yMax = 0

    private var origin: PointF = PointF()
    private var leftPoint: PointF = PointF()
    private var rightPoint: PointF = PointF()
    private var topPoint: PointF = PointF()
    private var bottomPoint: PointF = PointF()
    private var a: Float = 0f
    private var b: Float = 0f
    private var c: Float = 0f
    private var d: Float = 0f
    private var circular: CircularType.Circular? = null
    private var linearType: LinearType? = null
    private var xPointsValues: Array<PointF?>
    private val lines: MutableList<FunctionLine<*>> = ArrayList()
    private val points: MutableList<SinglePoint> = ArrayList()

    private var axisPaintedList: MutableList<PaintData> = ArrayList()

    init {
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = DimenUtil.dip2px(3f)
            isAntiAlias = true //开启抗锯齿
            isFilterBitmap = true
            isDither = true //开启防抖
        }

        drawPaint = Paint(paint)

        eraserPaint = Paint(paint)
        eraserPaint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            strokeWidth = DimenUtil.dip2px(8f)
        }

        textPaint = Paint(paint)
        textPaint.apply {
            color = Color.BLUE
            style = Paint.Style.FILL
            textSize = DimenUtil.sp2px(12f)
        }

        axisPaint.strokeWidth = axisWidth.toFloat()
        axisPaint.color = axisColor
        axisPaint.isAntiAlias = true
        axisPaint.style = Paint.Style.STROKE
        axisPaint.textSize = coordinateTextSize.toFloat()

        functionLinePaint.strokeWidth = functionLineWidth.toFloat()
        functionLinePaint.color = lineColor
        functionLinePaint.isAntiAlias = true
        functionLinePaint.isDither = true
        functionLinePaint.style = Paint.Style.STROKE

        pointPaint.color = DEFAULT_SINGLE_POINT_COLOR
        pointPaint.style = Paint.Style.FILL
        pointPaint.isAntiAlias = true

        xPointsValues = arrayOfNulls(segmentSize)

        setPaintMode(PEN)
        isDrawingCacheEnabled = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initAxisBoundaryPoint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!this::bitmap.isInitialized) {
            bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap).apply {
                drawFilter =
                    PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            }
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
                        saveDrawPointWithText(x, y)
                        if (clickTimes == 1) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            saveDrawPath(path, paint, mPaintedList)
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
                        saveDrawPointWithText(x, y)
                        when (clickTimes) {
                            1 -> {
                                path.moveTo(x, y)
                                preX = x
                                preY = y
                            }
                            2 -> {
                                path.lineTo(x, y)
                                saveDrawPath(path, paint, mPaintedList)
                            }
                            3 -> {
                                path.lineTo(x, y)
                                saveDrawPath(path, paint, mPaintedList)
                                path.lineTo(preX, preY)
                                path.close() // 使这些点构成封闭的多边形 
                                saveDrawPath(path, paint, mPaintedList)
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
                            saveDrawPointWithText(x, y)
                            preX = x
                            preY = y
                        } else if (clickTimes == 2) {
                            drawRectFRemainingPointsText(preX, preY, x, y)
                            val rectF = getRectF(preX, preY, x, y)
                            path.addRect(rectF, Path.Direction.CW)
                            saveDrawPath(path, paint, mPaintedList)
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
                            saveDrawPointWithText(x, y)
                            preX = x
                            preY = y
                        } else if (clickTimes == 2) {
                            val radius = sqrt((x - preX) * (x - preX) + (y - preY) * (y - preY))
                            path.addCircle(preX, preY, radius, Path.Direction.CW)
                            saveDrawPath(path, paint, mPaintedList)
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
            while (paintList.isNotEmpty()
                && (paintList.last().mType == DRAW_TYPE_POINT || paintList.last().mType == DRAW_TYPE_TEXT)
            ) {
                mRevokedList.add(paintList.removeLast())
            }
        } else {
            mPaintedList.add(lastPaint)
            while (paintList.isNotEmpty() && paintList.last().mType != DRAW_TYPE_POINT) {
                mPaintedList.add(paintList.removeLast())
            }
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        for (paintData in mPaintedList) {
            if (paintData.mType == DRAW_TYPE_TEXT) {
                paintData.drawText(canvas)
            } else {
                paintData.drawPath(canvas)
            }
        }
        invalidate()
    }

    private fun getPointText(): String = "P${++pointCount}"

    // 画出path并记录 path默认是线条
    private fun saveDrawPath(path: Path, paint: Paint, list: MutableList<PaintData>) {
        canvas.drawPath(path, paint)
        list.add(PaintData(Paint(paint), Path(path), null, DRAW_TYPE_LINE))
    }

    // 写出点的文本形式并记录(P1 P2...)
    private fun saveDrawPointWithText(x: Float, y: Float) {
        val path = Path()
        path.addCircle(x, y, DEFAULT_POINT_RADIUS.toFloat(), Path.Direction.CW)
        paint.style = Paint.Style.FILL
        saveDrawPoint(path, paint, mPaintedList)
        paint.style = Paint.Style.STROKE
        saveDrawText(x, y, getPointText(), textPaint, mPaintedList)
    }

    // 画出point并保存 (Point以Circle的path形式存在)
    private fun saveDrawPoint(path: Path, paint: Paint, list: MutableList<PaintData>) {
        canvas.drawPath(path, paint)
        list.add(PaintData(Paint(paint), Path(path), null, DRAW_TYPE_POINT))
    }

    // 画出text并保存
    private fun saveDrawText(
        x: Float,
        y: Float,
        text: String,
        paint: Paint,
        list: MutableList<PaintData>
    ) {
        canvas.drawText(text, x, y, paint)
        list.add(PaintData(Paint(paint), null, TextData(x, y, text), DRAW_TYPE_TEXT))
    }

    // 矩形除了第一个点击的点剩余的三个点
    private fun drawRectFRemainingPointsText(x1: Float, y1: Float, x2: Float, y2: Float) {
        saveDrawPointWithText(x1, y2)
        saveDrawPointWithText(x2, y1)
        saveDrawPointWithText(x2, y2)
    }

    // 根据两次点击点的坐标构造矩形
    private fun getRectF(x1: Float, y1: Float, x2: Float, y2: Float): RectF {
        if (x1 <= x2 && y1 >= y2) return RectF(x1, y2, x2, y1)
        if (x1 <= x2 && y1 <= y2) return RectF(x1, y1, x2, y2)
        if (x1 >= x2 && y1 >= y2) return RectF(x2, y2, x1, y1)
        if (x1 >= x2 && y1 <= y2) return RectF(x2, y1, x1, y2)
        return RectF()
    }

    // 初始化坐标轴边界点
    private fun initAxisBoundaryPoint() {
        width = measuredWidth.toFloat()
        height = measuredHeight.toFloat()
        origin.set(width / 2f, height / 2f)
        leftPoint.set(0f, height / 2f)
        rightPoint.set(width, height / 2f)
        topPoint.set(width / 2f, 0f)
        bottomPoint.set(width / 2f, height)
    }

    // 坐标轴描点
    private fun drawAxisPoint(point: SinglePoint) {
        val pointRaw = convertLogicalPoint2Raw(point.point, unitLength)
        if (point.pointColor != null) {
            pointPaint.color = point.pointColor!!
        }
        val radius =
            if (point.pointRadius == null) DEFAULT_SINGLE_POINT_RADIUS else point.pointRadius!!
        canvas.drawCircle(pointRaw.x, pointRaw.y, radius.toFloat(), pointPaint)
    }

    private fun drawFuncLine() {
        if (linearType != null) {
            when (linearType!!.javaClass.simpleName) {
                "LinearType" -> generateLinearLines(a, b)
                "PowerType" -> if (c == 1f) {
                    generateLinearLines(a, b)
                } else {
                    generatePowerLines(a, b, c)
                }
                "ExpType" -> when (c) {
                    1f -> {
                        generateLinearLines(0f, a!! + b!!)
                    }
                    0f -> {
                        generateLinearLines(0f, b)
                    }
                    else -> {
                        generateExpLines(a, b, c)
                    }
                }
                "LogType" -> generateLogLines(a, b, c, d)
                "CircularType" -> generateCircularLines(a, b, c, d, circular)
            }
        }
    }

    private fun generateLinearLines(a: Float?, b: Float?) {
        // raw
        val start = leftPoint
        val end = rightPoint
        // logical
        val startLogic = convertRawPoint2Logical(start, unitLength)
        val endLogic = convertRawPoint2Logical(end, unitLength)
        // calculate
        startLogic.y = FuncUtils.getLinearYValue(a!!, b!!, startLogic.x)
        endLogic.y = FuncUtils.getLinearYValue(a, b, endLogic.x)
        // convert logical to raw
        val startRaw = convertLogicalPoint2Raw(startLogic, unitLength)
        val endRaw = convertLogicalPoint2Raw(endLogic, unitLength)
        // draw lines
        canvas.drawLine(startRaw.x, startRaw.y, endRaw.x, endRaw.y, functionLinePaint)
    }

    private fun generatePowerLines(a: Float?, b: Float?, c: Float?) {
        // raw
        val start = leftPoint
        val end = rightPoint
        val unit = (end.x - start.x) / xPointsValues.size
        for (i in xPointsValues.indices) {
            // get the split point
            val split = PointF(start.x + i * unit, start.y)
            // logical
            val splitLogic = convertRawPoint2Logical(split, unitLength)
            // calculate
            splitLogic.y = FuncUtils.getPowYValue(a!!, b!!, c!!, splitLogic.x)
            // convert logical to raw
            val splitRaw = convertLogicalPoint2Raw(splitLogic, unitLength)
            xPointsValues[i] = splitRaw
        }
        drawBezier(canvas, FuncType.POWER_TYPE)
    }

    private fun generateExpLines(a: Float?, b: Float?, c: Float?) {
        // raw
        val start = leftPoint
        val end = rightPoint
        val unit = (end.x - start.x) / xPointsValues.size
        for (i in xPointsValues.indices) {
            // get the split point
            val split = PointF(start.x + i * unit, start.y)
            // logical
            val splitLogic = convertRawPoint2Logical(split, unitLength)
            // calculate
            splitLogic.y = FuncUtils.getExpYValue(a!!, b!!, c!!, splitLogic.x)
            // convert logical to raw
            val splitRaw = convertLogicalPoint2Raw(splitLogic, unitLength)
            xPointsValues[i] = splitRaw
        }
        drawBezier(canvas, FuncType.EXP_TYPE)
    }


    private fun generateLogLines(a: Float?, b: Float?, c: Float?, d: Float?) {
        // raw
        val start = PointF()
        start.set(origin)
        start.x += 1f
        val end = rightPoint
        val unit = (end.x - start.x) / xPointsValues.size
        for (i in xPointsValues.indices) {
            // get the split point
            val split = PointF(start.x + i * unit, start.y)
            // logical
            val splitLogic = convertRawPoint2Logical(split, unitLength)
            // calculate
            if (splitLogic.x == 0f) {
                continue
            }
            try {
                splitLogic.y = FuncUtils.getLogYValue(a!!, b!!, c!!, d!!, splitLogic.x)
            } catch (e: FunctionNotValidException) {
                continue
            }
            // convert logical to raw
            val splitRaw = convertLogicalPoint2Raw(splitLogic, unitLength)
            xPointsValues[i] = splitRaw
        }
        drawBezier(canvas, FuncType.LOG_TYPE)
    }

    private fun generateCircularLines(
        a: Float?,
        b: Float?,
        c: Float?,
        d: Float?,
        type: CircularType.Circular?
    ) {
        // raw
        val start = leftPoint
        val end = rightPoint
        val unit = (end.x - start.x) / xPointsValues.size
        for (i in xPointsValues.indices) {
            // get the split point
            val split = PointF(start.x + i * unit, start.y)
            // logical
            val splitLogic = convertRawPoint2Logical(split, unitLength)
            // calculate
            val y: Float = try {
                FuncUtils.getCircularYValue(a!!, b!!, c!!, d!!, splitLogic.x, type)
            } catch (e: FunctionTypeException) {
                continue
            }
            splitLogic.y = y
            // convert logical to raw
            val splitRaw = convertLogicalPoint2Raw(splitLogic, unitLength)
            xPointsValues[i] = splitRaw
        }
        drawBezier(canvas, FuncType.CIRCULAR_TYPE)
    }

    private fun drawBezier(canvas: Canvas, type: FuncType) {
        if (xPointsValues.isNotEmpty()) {
            val path = Path()
            var k = -xMax
            // if it is tangent function
            if (circular != null && circular == CircularType.Circular.TAN) {
                k = -xMax / 2
                while (k < 0) {
                    // from left to right
                    val leftX = k * PI - PI / 2
                    val rightX = k * PI + PI / 2
                    if (-xMax / 2 >= leftX && -xMax / 2 <= rightX) {
                        break
                    }
                    k++
                }
            }
            // if it is cotangent function
            if (circular != null && circular == CircularType.Circular.COT) {
                k = -xMax / 2
                while (k < 0) {
                    // from left to right
                    val leftX = k * PI - PI / 2
                    val rightX = k * PI + PI / 2
                    if (-xMax / 2 >= leftX && -xMax / 2 <= rightX) {
                        break
                    }
                    k++
                }
            }
            for (i in 0 until xPointsValues.size - 1) {
                // 超出屏幕范围的点 不会绘制曲线
                if (xPointsValues[i] != null && xPointsValues[i + 1] != null
                    && (xPointsValues[i]!!.y in 0.0..height.toDouble()
                            || i < xPointsValues.size - 1 && xPointsValues[i + 1]!!.y <= height && xPointsValues[i + 1]!!.y >= 0
                            || i > 0 && xPointsValues[i - 1]!!.y <= height && xPointsValues[i - 1]!!.y >= 0)
                ) {
                    path.moveTo(xPointsValues[i]!!.x, xPointsValues[i]!!.y)
                    // 接下来将会计算得到两个相邻的点的切线方程，由此再算出两条切线的交点，将这个交点作为贝塞尔曲线的控制点
                    val ad_x1 = xPointsValues[i]!!.x + dx
                    val dpLogic1 = convertRawPoint2Logical(ad_x1, origin.y, unitLength, origin)
                    val dp1 = FuncUtils.getPointByType(
                        a,
                        b,
                        c,
                        d,
                        dpLogic1.x,
                        type,
                        circular
                    ) ?: continue
                    // get a line near xPointsValues[i]
                    val tangentLineFuncCoefficients1 = FuncUtils.computeLinearFuncsByPoints(
                        convertRawPoint2Logical(xPointsValues[i]!!, unitLength), dp1
                    ) ?: return

                    // get a point on the line which super near the (current + 1) point
                    val ad_x2 = xPointsValues[i + 1]!!.x - dx
                    val dpLogic2 = convertRawPoint2Logical(ad_x2, origin.y, unitLength, origin)
                    val dp2 =
                        FuncUtils.getPointByType(a, b, c, d, dpLogic2.x, type, circular)
                    // get a line near xPointsValues[i + 1]
                    val tangentLineFuncCoefficients2 = FuncUtils.computeLinearFuncsByPoints(
                        convertRawPoint2Logical(xPointsValues[i + 1]!!, unitLength), dp2!!
                    ) ?: return

                    // if it is the tan func
                    if (circular != null && circular == CircularType.Circular.TAN) {
                        val domainLeft = k * PI - PI / 2
                        val domainRight = k * PI + PI / 2
                        if (dpLogic1.x > domainLeft && dpLogic1.x < domainRight) {
                            if (dpLogic2.x > domainRight) {
                                k++
                                continue
                            }
                        }
                    }

                    // if it is the cot func
                    if (circular != null && circular == CircularType.Circular.COT) {
                        var domain = k * PI
                        while (dpLogic1.x > domain) {
                            k++
                            domain = k * PI
                        }
                        if (dpLogic1.x < domain) {
                            if (dpLogic2.x > domain) {
                                k++
                                continue
                            }
                        }
                    }

                    // compute the intersection point as the control point of bezier curve
                    val controlPointLogic = FuncUtils.intersectionBetweenLinearFuncs(
                        tangentLineFuncCoefficients1[0],
                        tangentLineFuncCoefficients1[1],
                        tangentLineFuncCoefficients2[0],
                        tangentLineFuncCoefficients2[1]
                    ) ?: return
                    val controlPointRaw = convertLogicalPoint2Raw(controlPointLogic, unitLength)
                    path.quadTo(
                        controlPointRaw.x,
                        controlPointRaw.y,
                        xPointsValues[i + 1]!!.x,
                        xPointsValues[i + 1]!!.y
                    )
                    canvas.drawPath(path, functionLinePaint)
                }
            }
        }
    }

    @Throws(FunctionTypeException::class)
    private fun <T : LinearType?> setFunctionType(type: T?) {
        if (type != null) {
            when (type.javaClass.simpleName) {
                "LinearType" -> {
                    a = type.a
                    b = type.b
                    c = 0f
                    this.linearType = type
                }
                "PowerType" -> {
                    val powerType = type as PowerType
                    a = powerType.a
                    b = powerType.b
                    c = powerType.c
                    this.linearType = powerType
                }
                "ExpType" -> {
                    val expType = type as ExpType
                    a = expType.a
                    b = expType.b
                    c = expType.c
                    this.linearType = expType
                }
                "LogType" -> {
                    val logType = type as LogType
                    a = logType.a
                    b = logType.b
                    c = logType.c
                    d = logType.d
                    this.linearType = logType
                }
                "CircularType" -> {
                    val circularType = type as CircularType
                    a = circularType.a
                    b = circularType.b
                    c = circularType.c
                    d = circularType.d
                    circular = circularType.type
                    this.linearType = circularType
                }
                else -> throw FunctionTypeException("Function type error.")
            }
        }
    }

    private fun resetFuncStatus() {
        a = 0f
        b = 0f
        c = 0f
        d = 0f
        circular = null
        linearType = null
    }

    private fun convertLogicalPoint2Raw(logical: PointF, unitLength: Float): PointF {
        return convertLogicalPoint2Raw(logical.x, logical.y, unitLength, origin)
    }

    private fun convertLogicalPoint2Raw(
        x: Float,
        y: Float,
        unitLength: Float,
        origin: PointF
    ): PointF {
        val rawX = origin.x + x * unitLength
        val rawY = origin.y - y * unitLength
        return PointF(rawX, rawY)
    }

    private fun convertRawPoint2Logical(raw: PointF, unitLength: Float): PointF {
        return convertRawPoint2Logical(raw.x, raw.y, unitLength, origin)
    }

    private fun convertRawPoint2Logical(
        x: Float,
        y: Float,
        unitLength: Float,
        origin: PointF
    ): PointF {
        val logicalX = (x - origin.x) / unitLength
        val logicalY = (origin.y - y) / unitLength
        return PointF(logicalX, logicalY)
    }

    private fun drawAxis() {
        val path = Path()
        path.moveTo(leftPoint.x, leftPoint.y)
        path.lineTo(rightPoint.x, rightPoint.y)
        path.moveTo(topPoint.x, topPoint.y)
        path.lineTo(bottomPoint.x, bottomPoint.y)
        saveDrawPath(path, axisPaint, axisPaintedList)

        // y轴箭头
        axisPaint.style = Paint.Style.FILL
        path.moveTo(topPoint.x, topPoint.y)
        path.lineTo(topPoint.x - 10, topPoint.y + 20)
        path.lineTo(topPoint.x + 10, topPoint.y + 20)
        path.close()
        // x轴箭头
        path.moveTo(rightPoint.x, rightPoint.y)
        path.lineTo(rightPoint.x - 20, rightPoint.y - 10)
        path.lineTo(rightPoint.x - 20, rightPoint.y + 10)
        path.close()
        saveDrawPath(path, axisPaint, axisPaintedList)

        // 坐标点单位长度
        unitLength = if (width > height) height / 2 / (max + 1) else width / 2 / (max + 1)
        xMax = (if (width > height) width / unitLength else height / unitLength).toInt()
        if (xMax >= max) {
            yMax = max
        } else {
            yMax = xMax
            xMax = max
        }

        // x-
        for (i in 0 until xMax) {
            val x = origin.x - unitLength * (i + 1)
            val y = origin.y
            if (x > leftPoint.x) {
                path.moveTo(x, y)
                path.close()
                path.addCircle(x, y, axisPointRadius.toFloat(), Path.Direction.CW)
                val curText: String = (-(i + 1)).toString()
                saveDrawText(x, y + coordinateTextSize, curText, axisPaint, axisPaintedList)
            }
        }
        // x+
        for (i in 0 until xMax) {
            val x = origin.x + unitLength * (i + 1)
            val y = origin.y
            if (x < rightPoint.x) {
                path.moveTo(x, y)
                path.close()
                path.addCircle(x, y, axisPointRadius.toFloat(), Path.Direction.CW)
                val curText: String = (i + 1).toString()
                saveDrawText(x, y + coordinateTextSize, curText, axisPaint, axisPaintedList)
            }
        }
        // y+
        for (i in 0 until yMax) {
            val x = origin.x
            val y = origin.y - unitLength * (i + 1)
            if (y > topPoint.y) {
                path.moveTo(x, y)
                path.close()
                path.addCircle(x, y, axisPointRadius.toFloat(), Path.Direction.CW)
                val curText: String = (i + 1).toString()
                saveDrawText(x - coordinateTextSize, y, curText, axisPaint, axisPaintedList)
            }
        }
        // y-
        for (i in 0 until yMax) {
            val x = origin.x
            val y = origin.y + unitLength * (i + 1)
            if (y < bottomPoint.y) {
                path.moveTo(x, y)
                path.close()
                path.addCircle(x, y, axisPointRadius.toFloat(), Path.Direction.CW)
                val curText: String = (-(i + 1)).toString()
                saveDrawText(x - coordinateTextSize * 1.2f, y, curText, axisPaint, axisPaintedList)
            }
        }
        saveDrawPoint(path, axisPaint, axisPaintedList)
        axisPaint.style = Paint.Style.STROKE
        showAxis = true
        axisPaintedList.clear() // 暂时用不上，保持save方法使用规范就先add再clear
        invalidate()
    }

    private fun clearAxis() {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        for (paintData in mPaintedList) {
            if (paintData.mType == DRAW_TYPE_TEXT) {
                paintData.drawText(canvas)
            } else {
                paintData.drawPath(canvas)
            }
        }
        showAxis = false
        invalidate()
    }

    fun addFunctionLine(line: FunctionLine<*>) {
        //lines.add(line)
        linearType = line.functionType
        if (line.lineColor != null) {
            functionLinePaint.color = line.lineColor!!
        } else {
            functionLinePaint.color = lineColor
        }
        if (line.lineWidth != null) {
            functionLinePaint.strokeWidth = line.lineWidth!!.toFloat()
        } else {
            functionLinePaint.strokeWidth = functionLineWidth.toFloat()
        }
        try {
            resetFuncStatus()
            setFunctionType(line.functionType)
            drawFuncLine()
            invalidate()
        } catch (e: FunctionTypeException) {
            e.printStackTrace()
        }
    }

    fun addPoint(point: SinglePoint) {
        //points.add(point)
        drawAxisPoint(point)
        invalidate()
    }

    fun reset() {
        lines.clear()
        points.clear()
        invalidate()
    }

    fun setAxisWidth(axisWidth: Int) {
        this.axisWidth = axisWidth
    }

    fun setPrecision(precision: Int) {
        this.dx = precision
    }

    fun setSegmentSize(segmentSize: Int) {
        this.segmentSize = segmentSize
    }

    fun setAxisColor(axisColor: Int) {
        this.axisColor = axisColor
    }

    fun setAxisPointRadius(axisPointRadius: Int) {
        this.axisPointRadius = axisPointRadius
    }

    fun setMax(max: Int) {
        this.max = max
    }

    fun setPaintMode(mode: Int) {
        paintMode = mode
        paint = if (mode == ERASER) eraserPaint else drawPaint
        path.reset()
        clickTimes = 0
    }

    fun setPaintColor(color: Int) {
        drawPaint.color = color
    }

    fun getPaintColor(): Int {
        return drawPaint.color
    }

    fun clearDraw() {
        path.reset()
        mPaintedList.clear()
        mRevokedList.clear()
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        clickTimes = 0
        pointCount = 0
        clearAxis()
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

    fun drawOrHideAxis() {
        showAxis = !showAxis
        if (showAxis) drawAxis() else clearAxis()
    }

    companion object {
        const val TAG = "BoardView"
        const val DRAW_TYPE_POINT = 1
        const val DRAW_TYPE_LINE = 2
        const val DRAW_TYPE_TEXT = 3
        private const val DEFAULT_POINT_RADIUS = 7
        private const val PI = Math.PI.toFloat()
        private const val DEFAULT_AXIS_WIDTH = 2
        private const val DEFAULT_FUNCTION_LINE_WIDTH = 3
        private const val DEFAULT_COORDINATE_TEXT_SIZE = 16
        private const val DEFAULT_SEGMENT_SIZE = 50
        private const val DEFAULT_PRECISION = 1
        private const val DEFAULT_AXIS_POINT_RADIUS = 5
        private const val DEFAULT_AXIS_COLOR = Color.BLACK
        private const val DEFAULT_MAX = 12
        private const val DEFAULT_SINGLE_POINT_RADIUS = 8
        private const val DEFAULT_SINGLE_POINT_COLOR = DEFAULT_AXIS_COLOR
    }
}


data class PaintData(
    val mPaint: Paint,
    val mPath: Path? = null,
    val mText: TextData? = null,
    val mType: Int = DRAW_TYPE_LINE
) {

    fun drawPath(canvas: Canvas) {
        canvas.drawPath(mPath!!, mPaint)
    }

    fun drawText(canvas: Canvas) {
        canvas.drawText(mText!!.text, mText.x, mText.y, mPaint)
    }
}

data class TextData(val x: Float, val y: Float, val text: String)