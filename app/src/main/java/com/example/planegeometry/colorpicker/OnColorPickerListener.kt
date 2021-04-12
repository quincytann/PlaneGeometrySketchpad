package com.example.planegeometry.colorpicker

interface OnColorPickerListener {
    fun onColorCancel(dialog: ColorPickerDialog?)
    fun onColorChange(dialog: ColorPickerDialog?, color: Int)
    fun onColorConfirm(dialog: ColorPickerDialog?, color: Int)
}