package com.example.planegeometry.coordinateaxischart

import com.example.planegeometry.coordinateaxischart.type.LinearType

/**
 * Created by KiBa-PC on 2017/4/21.
 */
class FunctionLine<T : LinearType?> {
    var functionType: T
    var lineColor: Int? = null
        private set
    var lineWidth: Int? = null
        private set

    constructor(functionType: T) {
        this.functionType = functionType
    }

    constructor(functionType: T, lineColor: Int) {
        this.functionType = functionType
        this.lineColor = lineColor
    }

    constructor(functionType: T, lineColor: Int, lineWidth: Int) {
        this.functionType = functionType
        this.lineColor = lineColor
        this.lineWidth = lineWidth
    }

    fun setLineColor(lineColor: Int) {
        this.lineColor = lineColor
    }

    fun setLineWidth(lineWidth: Int) {
        this.lineWidth = lineWidth
    }
}