package com.ansgar.swipeableview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Copyright (c) 2018 SwipeableView. All rights reserved.
 */
class SwipeableView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val TAG: String = SwipeableView::class.java.simpleName
    private var onChangeListener: OnChangeListener? = null
    private var yOffset: Float = 0f
    private var xOffset: Float = 0f
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    var bottomIv: ImageView? = null
    var editText: EditText? = null

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeableView, 0, 0)
        val animatedIvResId = typedArray.getResourceId(R.styleable.SwipeableView_image_view_id, -1)
//        bottomIv = findViewById(animatedIvResId)
//        typedArray.recycle()
        screenWidth = getScreenWidth()
        screenHeight = getScreenHeight()
    }

    /**
     * To pass selected image
     */
    fun updateUi() {

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val rawTouchX: Float = event.rawX
        val rawTouchY: Float = event.rawY
        val touchX: Float = event.x
        val touchY: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(touchX, touchY)
            MotionEvent.ACTION_MOVE -> onActionMove(rawTouchX, rawTouchY)
            MotionEvent.ACTION_UP -> onActionUp(rawTouchX, rawTouchY)
        }
        return true
    }

    private fun onActionDown(x: Float, y: Float) {
        yOffset = y
    }

    private fun onActionMove(x: Float, y: Float) {
        val params: ViewGroup.LayoutParams = layoutParams
        params.height = (screenHeight - y + yOffset).toInt()
        this.layoutParams = layoutParams

        moveBottomIv()
    }

    private fun onActionUp(rawTouchX: Float, rawTouchY: Float) {

    }

    private fun moveBottomIv() {
        val viewPosPercent: Double = height.toDouble() / getScreenHeight()
        val animatedIvPos: Float = (width * viewPosPercent / 1.2).toFloat()
        bottomIv?.x = animatedIvPos
        animateEditText(animatedIvPos)
    }

    private fun animateEditText(value: Float) {
        val alpha = value / 1000
        Log.i(TAG, "Alpha: $alpha")
        if (alpha < 0.15) editText?.visibility = View.GONE
        else editText?.visibility = View.VISIBLE

        editText?.alpha = alpha * 2
    }

    private fun getDisplayMetric(): DisplayMetrics {
        val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics: DisplayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        return displayMetrics
    }

    private fun getScreenHeight(): Int {
        return getDisplayMetric().heightPixels
    }

    private fun getScreenWidth(): Int {
        return getDisplayMetric().widthPixels
    }

    interface OnChangeListener {
        fun onViewCreated()

        fun onViewDismissed()

        fun onContentChanged()
    }

}