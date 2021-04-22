package com.example.planegeometry.coordinateaxischart.type

/**
 * a,b,c的值必须按照如下公式进行传递：
 * y = a * log(c * x + d) + b
 */
open class LogType(a: Float, b: Float, c: Float, var d: Float) : PowerType(a, b, c)