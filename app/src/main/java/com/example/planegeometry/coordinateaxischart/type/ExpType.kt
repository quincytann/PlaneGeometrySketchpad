package com.example.planegeometry.coordinateaxischart.type

/**
 * a,b,c的值必须按照如下公式进行传递：
 * y = a * pow(c, x) + b
 */
class ExpType(a: Float, b: Float, c: Float) : PowerType(a, b, c)