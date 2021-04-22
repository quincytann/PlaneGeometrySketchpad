package com.example.planegeometry.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.planegeometry.R
import com.example.planegeometry.utils.CLog
import com.example.planegeometry.utils.MyApplication
import com.example.planegeometry.utils.ProxyClickListener
import kotlinx.android.synthetic.main.menu_view_layout.view.*

class MenuView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val mItemsView = mutableListOf<ViewGroup>()

    init {
        val view = LayoutInflater.from(MyApplication.context).inflate(R.layout.menu_view_layout, this, true)
        mItemsView.apply {
            add(view.findViewById(R.id.item_pen))
            add(view.findViewById(R.id.item_color))
            add(view.findViewById(R.id.item_clear))
            add(view.findViewById(R.id.item_eraser))
            add(view.findViewById(R.id.item_revoke))
            add(view.findViewById(R.id.item_undo))
            add(view.findViewById(R.id.item_coordinate))
            add(view.findViewById(R.id.item_segment))
            add(view.findViewById(R.id.item_triangle))
            add(view.findViewById(R.id.item_rectangular))
            add(view.findViewById(R.id.item_circle))
            add(view.findViewById(R.id.item_save))
            add(view.findViewById(R.id.item_share))
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    fun setClickItemCallBack(clickListeners: List<() -> Unit>) {
        for (id in 0 until mItemsView.size) {
            clickListeners.getOrNull(id)?.let { callback ->
                mItemsView.getOrNull(id)?.let {
                    it.setOnClickListener(ProxyClickListener {
                        callback.invoke()
                        updateSelectedStatus(id)
                    })
                }
            }
        }
    }

    private fun updateSelectedStatus(selectedId: Int) {
        if (useIndependentBackground(selectedId)) {
            return
        }
        for (id in 0 until mItemsView.size) {
            if (useIndependentBackground(id)) {
                continue
            } else {
                if (selectedId == id) {
                    mItemsView[id].setBackgroundResource(R.drawable.menu_items_selected_bg)
                } else {
                    mItemsView[id].setBackgroundResource(0)
                }
            }
        }
    }

    // 使用单独点击背景的item
    private fun useIndependentBackground(id: Int): Boolean {
        return id == CLEAR || id == REVOKE || id == UNDO || id == SAVE || id == SHARE || id == COLOR || id == COORDINATE
    }

    companion object {
        const val TAG = "MenuView"

        const val PEN = 0
        const val COLOR = 1
        const val CLEAR = 2
        const val ERASER = 3
        const val REVOKE = 4
        const val UNDO = 5
        const val COORDINATE = 6
        const val SEGMENT = 7
        const val TRIANGLE = 8
        const val RECTANGULAR = 9
        const val CIRCLE = 10
        const val SAVE = 11
        const val SHARE = 12
    }

}