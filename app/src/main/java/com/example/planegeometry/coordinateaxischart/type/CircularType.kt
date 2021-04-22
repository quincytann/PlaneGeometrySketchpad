package com.example.planegeometry.coordinateaxischart.type

/**
 * a,b,c的值必须按照如下公式进行传递：
 * y = a * sin(cx + d) + b
 * y = a * cos(cx + d) + b
 * y = a * tan(cx + d) + b
 * y = a * cot(cx + d) + b
 */
class CircularType(a: Float, b: Float, c: Float, d: Float, var type: Circular) :
    LogType(a, b, c, d) {

    enum class Circular {
        SIN,  // sin
        COS,  // cos
        TAN,  // tan
        COT // cot
    }
}