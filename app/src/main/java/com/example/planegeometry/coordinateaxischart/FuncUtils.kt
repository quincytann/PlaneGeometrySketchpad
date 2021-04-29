package com.example.planegeometry.coordinateaxischart

import android.graphics.Color
import android.graphics.PointF
import com.example.planegeometry.coordinateaxischart.exception.FunctionNotValidException
import com.example.planegeometry.coordinateaxischart.exception.FunctionTypeException
import com.example.planegeometry.coordinateaxischart.type.*
import com.example.planegeometry.coordinateaxischart.type.CircularType.Circular
import com.example.planegeometry.funtionInput.FunctionInputData
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.COS
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.COT
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.EXP
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.LINEAR
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.LOG
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.POWER
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.SIN
import com.example.planegeometry.funtionInput.FunctionInputDialog.Companion.TAN
import java.util.*
import kotlin.random.Random.Default.nextInt

object FuncUtils {
    @JvmStatic
    fun intersectionBetweenLinearFuncs(a1: Float, b1: Float, a2: Float, b2: Float): PointF? {
        if (a1 != a2) {
            val x = (b2 - b1) / (a1 - a2)
            val y = a1 * x + b1
            return PointF(x, y)
        }
        return null
    }

    @JvmStatic
    fun computeLinearFuncsByPoints(p1: PointF, p2: PointF): FloatArray? {
        if (p1 != p2) {
            val a = (p1.y - p2.y) / (p1.x - p2.x)
            val b = p1.y - a * p1.x
            return floatArrayOf(a, b)
        }
        return null
    }

    @JvmStatic
    fun getLinearYValue(a: Float, b: Float, x: Float): Float {
        return a * x + b
    }

    @JvmStatic
    fun getPowYValue(a: Float, b: Float, c: Float, x: Float): Float {
        return (a * Math.pow(x.toDouble(), c.toDouble()) + b).toFloat()
    }

    @JvmStatic
    fun getExpYValue(a: Float, b: Float, c: Float, x: Float): Float {
        return (a * Math.pow(c.toDouble(), x.toDouble()) + b).toFloat()
    }

    @JvmStatic
    @Throws(FunctionNotValidException::class)
    fun getLogYValue(a: Float, b: Float, c: Float, d: Float, x: Float): Float {
        if (c * x + d <= 0) {
            throw FunctionNotValidException("The value inside log() cannot be 0 or negative.")
        }
        return (a * Math.log((c * x + d).toDouble()) + b).toFloat()
    }

    @JvmStatic
    @Throws(FunctionTypeException::class)
    fun getCircularYValue(
        a: Float,
        b: Float,
        c: Float,
        d: Float,
        x: Float,
        type: Circular?
    ): Float {
        return when (type) {
            Circular.SIN -> (a * Math.sin((c * x + d).toDouble()) + b).toFloat()
            Circular.COS -> (a * Math.cos((c * x + d).toDouble()) + b).toFloat()
            Circular.TAN -> (a * Math.tan((c * x + d).toDouble()) + b).toFloat()
            Circular.COT -> {
                val tan = (a * Math.tan((c * x + d).toDouble()) + b).toFloat()
                if (tan != 0f) {
                    1 / (a * Math.tan((c * x + d).toDouble()) + b).toFloat()
                } else {
                    throw FunctionTypeException("cot(kπ) {n∈Z} is not valid.")
                }
            }
            else -> throw FunctionTypeException("No 'Circular Type' found.")
        }
    }

    @JvmStatic
    fun getPointByType(
        a: Float,
        b: Float,
        c: Float,
        d: Float,
        x: Float,
        type: FuncType?,
        circular: Circular?
    ): PointF? {
        when (type) {
            FuncType.LINEAR_TYPE -> return PointF(x, getLinearYValue(a, b, x))
            FuncType.POWER_TYPE -> return PointF(x, getPowYValue(a, b, c, x))
            FuncType.EXP_TYPE -> return PointF(x, getExpYValue(a, b, c, x))
            FuncType.LOG_TYPE -> try {
                return PointF(x, getLogYValue(a, b, c, d, x))
            } catch (e: FunctionNotValidException) {
                e.printStackTrace()
            }
            FuncType.CIRCULAR_TYPE -> try {
                return PointF(x, getCircularYValue(a, b, c, d, x, circular))
            } catch (e: FunctionTypeException) {
                e.printStackTrace()
            }
        }
        return null
    }

    @JvmStatic
    fun getFunctionLineByInputData(data: FunctionInputData): FunctionLine<*> {
        when (data.functionType) {
            LINEAR -> {
                return FunctionLine(LinearType(data.a, data.b), getRandomColor())
            }
            POWER -> {
                return FunctionLine(PowerType(data.a, data.b, data.c), getRandomColor())
            }
            EXP -> {
                return FunctionLine(ExpType(data.a, data.b, data.c), getRandomColor())
            }
            LOG -> {
                return FunctionLine(LogType(data.a, data.b, data.c, data.d), getRandomColor())
            }
            SIN -> {
                return FunctionLine(
                    CircularType(data.a, data.b, data.c, data.d, Circular.SIN),
                    getRandomColor()
                )
            }
            COS -> {
                return FunctionLine(
                    CircularType(data.a, data.b, data.c, data.d, Circular.COS),
                    getRandomColor()
                )
            }
            TAN -> {
                return FunctionLine(
                    CircularType(data.a, data.b, data.c, data.d, Circular.TAN),
                    getRandomColor()
                )
            }
            COT -> {
                return FunctionLine(
                    CircularType(data.a, data.b, data.c, data.d, Circular.COT),
                    getRandomColor()
                )
            }
            else -> {
                throw FunctionTypeException("Function Input data error.")
            }
        }
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

}