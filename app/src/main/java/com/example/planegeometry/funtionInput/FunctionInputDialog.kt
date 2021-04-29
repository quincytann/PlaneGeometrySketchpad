package com.example.planegeometry.funtionInput

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.planegeometry.R

class FunctionInputDialog(
    private val context: Context,
    private val mListener: OnFunctionInputListener?
) {

    private var mAlertDialog: AlertDialog = AlertDialog.Builder(context).create()
    private val mContentView: View =
        LayoutInflater.from(context).inflate(R.layout.input_function_dialog_layout, null)
    private val entry_a = mContentView.findViewById<LinearLayout>(R.id.entry_a)
    private val entry_b = mContentView.findViewById<LinearLayout>(R.id.entry_b)
    private val entry_c = mContentView.findViewById<LinearLayout>(R.id.entry_c)
    private val entry_d = mContentView.findViewById<LinearLayout>(R.id.entry_d)
    private val parameterA = mContentView.findViewById<EditText>(R.id.parameter_a)
    private val parameterB = mContentView.findViewById<EditText>(R.id.parameter_b)
    private val parameterC = mContentView.findViewById<EditText>(R.id.parameter_c)
    private val parameterD = mContentView.findViewById<EditText>(R.id.parameter_d)


    private lateinit var spinner: Spinner
    private var mFunctionType = 0  // 默认Linear

    init {
        initSpinner()
        mAlertDialog.apply {
            setCancelable(false)
            setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.resources.getString(R.string.dialog_positive)
            ) { _, _ ->
                handleInput()
                this.dismiss()
            }
            setButton(
                DialogInterface.BUTTON_NEGATIVE,
                context.resources.getString(R.string.dialog_negative)
            ) { _, _ ->
                this.dismiss()
            }
            setView(mContentView, 16, 16, 16, 16)
        }
    }

    private fun initSpinner() {
        spinner = mContentView.findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
            context,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mFunctionType = position
                updateInputBox(mFunctionType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun updateInputBox(type: Int) {
        when (type) {
            LINEAR -> {
                entry_a.visibility = View.VISIBLE
                entry_b.visibility = View.VISIBLE
                entry_c.visibility = View.GONE
                entry_d.visibility = View.GONE
            }
            POWER, EXP -> {
                entry_a.visibility = View.VISIBLE
                entry_b.visibility = View.VISIBLE
                entry_c.visibility = View.VISIBLE
                entry_d.visibility = View.GONE
            }
            LOG, SIN, COS, TAN, COT -> {
                entry_a.visibility = View.VISIBLE
                entry_b.visibility = View.VISIBLE
                entry_c.visibility = View.VISIBLE
                entry_d.visibility = View.VISIBLE
            }
        }
    }

    private fun handleInput() {
        var a = parameterA.text.toString().toFloatOrNull()
        var b = parameterB.text.toString().toFloatOrNull()
        var c = parameterC.text.toString().toFloatOrNull()
        var d = parameterD.text.toString().toFloatOrNull()
        if (!checkParameterValidity(a, b, c, d)) {
            mListener?.onFunctionParametersFixed(null)
        } else {
            if (a == null) a = 0f
            if (b == null) b = 0f
            if (c == null) c = 0f
            if (d == null) d = 0f
            mListener?.onFunctionParametersFixed(FunctionInputData(mFunctionType, a, b, c, d))
        }
    }

    private fun checkParameterValidity(a: Float?, b: Float?, c: Float?, d: Float?): Boolean {
        when (mFunctionType) {
            LINEAR -> {
                if (a == null || b == null) return false
            }
            POWER, EXP -> {
                if (a == null || b == null || c == null) return false
            }
            LOG, SIN, COS, TAN, COT -> {
                if (a == null || b == null || c == null || d == null) return false
            }
        }
        return true
    }

    fun show(): FunctionInputDialog {
        mAlertDialog.show()
        return this
    }

    companion object {
        const val LINEAR = 0
        const val POWER = 1
        const val EXP = 2
        const val LOG = 3
        const val SIN = 4
        const val COS = 5
        const val TAN = 6
        const val COT = 7
    }

}