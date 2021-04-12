package com.example.planegeometry.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


class ColorPlateView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mPaint: Paint? = null
    private var mShaderVertical: LinearGradient? = null
    private val HSV = floatArrayOf(1f, 1f, 1f)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mPaint == null) {
            mPaint = Paint()
            mShaderVertical = LinearGradient(
                0f,
                0f,
                0f,
                this.measuredHeight.toFloat(),
                -0x1,
                -0x1000000,
                Shader.TileMode.CLAMP
            ) //线性渐变
        }
        val rgb = Color.HSVToColor(HSV)
        val shaderHorizontal: LinearGradient =
            LinearGradient(0f, 0f, this.measuredWidth.toFloat(), 0f, -0x1, rgb, Shader.TileMode.CLAMP)
        val composeShader =
            ComposeShader(mShaderVertical!!, shaderHorizontal, PorterDuff.Mode.MULTIPLY) //混合渐变
        mPaint!!.shader = composeShader
        canvas.drawRect(
            0f,
            0f,
            this.measuredWidth.toFloat(),
            this.measuredHeight.toFloat(),
            mPaint!!
        )
    }

    /**
     * 设置色彩
     *
     * @param hue
     */
    fun setHue(hue: Float) {
        HSV[0] = hue
        invalidate()
    }

    companion object {
        private val TAG = ColorPlateView::class.java.name
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
}