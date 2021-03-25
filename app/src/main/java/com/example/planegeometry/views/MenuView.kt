package com.example.planegeometry.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.planegeometry.R
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
            add(view.findViewById(R.id.item_clear))
            add(view.findViewById(R.id.item_segment))
            add(view.findViewById(R.id.item_triangle))
            add(view.findViewById(R.id.item_rectangular))
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    fun setClickItemCallBack(clickListeners: List<() -> Unit>) {
        for (id in 0 until mItemsView.size) {
            clickListeners.getOrNull(id)?.let { callback ->
                mItemsView.getOrNull(id)?.let {
                    it.setOnClickListener(ProxyClickListener{
                        callback.invoke()
                        updateSelectedStatus(id)
                    })
                }
            }
        }
    }

    private fun updateSelectedStatus(selectedId: Int) {
        for (id in 0 until mItemsView.size) {
            if (id == selectedId) {
                mItemsView[id].setBackgroundResource(R.drawable.menu_items_selected_bg)
            } else {
                mItemsView[id].setBackgroundResource(0)
            }
        }
    }

    companion object {
        const val PEN = 0
        const val CLEAR = 1
        const val SEGMENT = 2
        const val TRIANGLE = 3
        const val RECTANGULAR = 4
    }

}