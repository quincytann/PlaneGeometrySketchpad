package com.example.planegeometry.colorpicker

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.planegeometry.R
import kotlin.math.floor
import kotlin.math.roundToInt

class ColorPickerDialog(
    context: Context,
    private var defaultColor: Int,
    private val mIsSupportAlpha: Boolean,
    private val mListener: OnColorPickerListener?
) {
    private lateinit var mAlertDialog: AlertDialog
    private val mViewContainer: ViewGroup
    private val mViewPlate: ColorPlateView
    private val mViewHue: View
    private val mViewAlphaBottom: ImageView
    private val mViewAlphaOverlay: View
    private val mPlateCursor: ImageView
    private val mHueCursor: ImageView
    private val mAlphaCursor: ImageView
    private val mViewOldColor: View
    private val mViewNewColor: View
    private val mCurrentHSV = FloatArray(3)
    private var alpha: Int


    /**
     * 触摸监听
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initOnTouchListener() {
        // 色彩板的触摸监听
        mViewHue.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val action: Int = event.action
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
                    var y = event.y
                    if (y < 0f) y = 0f
                    if (y > mViewHue.measuredHeight) y = mViewHue.measuredHeight - 0.001f
                    var colorHue = 360f - 360f / mViewHue.measuredHeight * y
                    if (colorHue == 360f) colorHue = 0f
                    mViewPlate.setHue(colorHue)
                    moveHueCursor()
                    this@ColorPickerDialog.colorHue = colorHue
                    mViewNewColor.setBackgroundColor(color)
                    mListener?.onColorChange(this@ColorPickerDialog, color)
                    updateAlphaView()
                    return true
                }
                return false
            }
        })


        // 支持透明度时的触摸监听
        if (mIsSupportAlpha) mViewAlphaBottom.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val action: Int = event.action
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
                    var y: Float = event.y
                    if (y < 0f) y = 0f
                    if (y > mViewHue.measuredHeight) y = mViewHue.measuredHeight - 0.001f
                    val alpha = (255f - 255f / mViewAlphaBottom.measuredHeight * y).roundToInt()
                    this@ColorPickerDialog.alpha = alpha
                    moveAlphaCursor()
                    mViewNewColor.setBackgroundColor(color)
                    mListener?.onColorChange(this@ColorPickerDialog, color)
                    return true
                }
                return false
            }
        })

        // 颜色样板的触摸监听
        mViewPlate.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val action: Int = event.action
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
                    var x: Float = event.x
                    var y: Float = event.y
                    if (x < 0f) x = 0f
                    if (x > mViewPlate.measuredWidth) x = mViewPlate.measuredWidth.toFloat()
                    if (y < 0f) y = 0f
                    if (y > mViewPlate.measuredHeight) y = mViewPlate.measuredHeight.toFloat()
                    colorSat = 1f / mViewPlate.measuredWidth * x //颜色深浅
                    colorVal = 1f - 1f / mViewPlate.measuredHeight * y //颜色明暗
                    movePlateCursor()
                    mViewNewColor.setBackgroundColor(color)
                    mListener?.onColorChange(this@ColorPickerDialog, color)
                    return true
                }
                return false
            }
        })
    }

    /**
     * 初始化AlerDialog
     */
    private fun initAlertDialog(context: Context, view: View) {
        mAlertDialog = AlertDialog.Builder(context).create()
        mAlertDialog.apply {
            setTitle(context.resources.getString(R.string.dialog_title))
            setCancelable(false)
            setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.resources.getString(R.string.dialog_positive)
            ) { _, _ ->
                mListener?.onColorConfirm(this@ColorPickerDialog, color)
                mAlertDialog.dismiss()
            }
        }
        mAlertDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            context.resources.getString(R.string.dialog_negative)
        ) { _, _ ->
            mListener?.onColorCancel(this@ColorPickerDialog)
            mAlertDialog.dismiss()
        }
        mAlertDialog.setView(view, 0, 0, 0, 0)
    }

    /**
     * 全局布局状态监听
     */
    private fun initGlobalLayoutListener(view: View) {
        val vto: ViewTreeObserver = view.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN) //api 16
            override fun onGlobalLayout() {
                moveHueCursor()
                movePlateCursor()
                if (mIsSupportAlpha) {
                    moveAlphaCursor()
                    updateAlphaView()
                }
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    /**
     * 移动色彩样板指针
     */
    private fun moveHueCursor() {
        var y = mViewHue.measuredHeight - colorHue * mViewHue.measuredHeight / 360f
        if (y == mViewHue.measuredHeight.toFloat()) y = 0f
        val layoutParams = mHueCursor.layoutParams as RelativeLayout.LayoutParams
        layoutParams.leftMargin =
            (mViewHue.left - floor((mHueCursor.measuredWidth / 3).toDouble()) - mViewContainer.paddingLeft).toInt()
        layoutParams.topMargin =
            (mViewHue.top + y - floor((mHueCursor.measuredHeight / 2).toDouble()) - mViewContainer.paddingTop).toInt()
        mHueCursor.layoutParams = layoutParams
    }

    /**
     * 移动透明度板的指针
     */
    private fun moveAlphaCursor() {
        val y = mViewAlphaBottom.measuredHeight - alpha * mViewAlphaBottom.measuredHeight / 255f
        val layoutParams: RelativeLayout.LayoutParams =
            mAlphaCursor.layoutParams as RelativeLayout.LayoutParams
        layoutParams.leftMargin =
            (mViewAlphaBottom.left - floor((mAlphaCursor.measuredWidth / 3).toDouble()) - mViewContainer.paddingLeft).toInt()
        layoutParams.topMargin =
            (mViewAlphaBottom.top + y - floor((mAlphaCursor.measuredHeight / 2).toDouble()) - mViewContainer.paddingTop).toInt()
        mAlphaCursor.layoutParams = layoutParams
    }

    /**
     * 移动最终颜色样板指针
     */
    private fun movePlateCursor() {
        val x = colorSat * mViewPlate.measuredWidth
        val y = (1f - colorVal) * mViewPlate.measuredHeight
        val layoutParams: RelativeLayout.LayoutParams =
            mPlateCursor.layoutParams as RelativeLayout.LayoutParams
        layoutParams.leftMargin =
            (mViewPlate.left + x - floor((mPlateCursor.measuredWidth / 2).toDouble()) - mViewContainer.paddingLeft).toInt()
        layoutParams.topMargin =
            (mViewPlate.top + y - floor((mPlateCursor.measuredHeight / 2).toDouble()) - mViewContainer.paddingTop).toInt()
        mPlateCursor.layoutParams = layoutParams
    }

    /**
     * 设置色彩
     *
     * @param color
     */
    private var colorHue: Float
        get() = mCurrentHSV[0]
        private set(color) {
            mCurrentHSV[0] = color
        }

    /**
     * 设置颜色深浅
     */
    private var colorSat: Float
        get() = mCurrentHSV[1]
        private set(color) {
            mCurrentHSV[1] = color
        }

    /**
     * 设置颜色明暗
     */
    private var colorVal: Float
        get() = mCurrentHSV[2]
        private set(color) {
            mCurrentHSV[2] = color
        }

    /**
     * 获取int颜色
     */
    private val color: Int
        get() {
            val argb = Color.HSVToColor(mCurrentHSV)
            return alpha shl 24 or (argb and 0x00ffffff)
        }

    /**
     * 更新透明度UI
     */
    private fun updateAlphaView() {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.HSVToColor(mCurrentHSV), 0x0)
        )
        mViewAlphaOverlay.setBackgroundDrawable(gd)
    }

    fun setButtonTextColor(color: Int) {
        val btnPositive: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative: Button = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        btnPositive.setTextColor(color)
        btnNegative.setTextColor(color)
    }

    fun show(): ColorPickerDialog {
        mAlertDialog.show()
        return this@ColorPickerDialog
    }

    val dialog: AlertDialog?
        get() = mAlertDialog

    companion object {
        private val TAG = ColorPickerDialog::class.java.name
    }

    /**
     * 创建支持透明度的取色器
     *
     * @param context        宿主Activity
     * @param defauleColor   默认的颜色
     * @param isSupportAlpha 颜色是否支持透明度
     * @param listener       取色器的监听器
     */
    init {
        if (!mIsSupportAlpha) {
            defaultColor = defaultColor or -0x1000000
        }
        Color.colorToHSV(defaultColor, mCurrentHSV)
        alpha = Color.alpha(defaultColor)
        val view: View = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null)
        mViewHue = view.findViewById(R.id.img_hue)
        mViewPlate = view.findViewById<View>(R.id.color_plate) as ColorPlateView
        mHueCursor = view.findViewById<View>(R.id.hue_cursor) as ImageView
        mViewOldColor = view.findViewById(R.id.view_old_color)
        mViewNewColor = view.findViewById(R.id.view_new_color)
        mPlateCursor = view.findViewById<View>(R.id.plate_cursor) as ImageView
        mViewContainer = view.findViewById<View>(R.id.container) as ViewGroup
        mViewAlphaOverlay = view.findViewById(R.id.view_overlay)
        mAlphaCursor = view.findViewById<View>(R.id.alpha_Cursor) as ImageView
        mViewAlphaBottom = view.findViewById<View>(R.id.img_alpha_bottom) as ImageView

        mViewAlphaBottom.visibility =
            if (mIsSupportAlpha) View.VISIBLE else View.GONE
        mViewAlphaOverlay.visibility =
            if (mIsSupportAlpha) View.VISIBLE else View.GONE
        mAlphaCursor.visibility = if (mIsSupportAlpha) View.VISIBLE else View.GONE

        mViewPlate.setHue(colorHue)
        mViewOldColor.setBackgroundColor(defaultColor)
        mViewNewColor.setBackgroundColor(defaultColor)
        initOnTouchListener()
        initAlertDialog(context, view)
        initGlobalLayoutListener(view)
    }
}