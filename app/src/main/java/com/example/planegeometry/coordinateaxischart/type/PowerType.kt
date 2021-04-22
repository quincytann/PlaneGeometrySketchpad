package com.example.planegeometry.coordinateaxischart.type

/**
 * a,b,c的值必须按照如下公式进行传递：
 * y = a * pow(x, c) + b
 */
open class PowerType(a: Float, b: Float, var c: Float) : LinearType(a, b)