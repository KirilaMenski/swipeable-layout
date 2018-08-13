package com.ansgar.swipeableview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import java.lang.ref.WeakReference

/**
 * Copyright (c) 2018 SwipeableView. All rights reserved.
 */
class SwipeableView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val TAG: String = SwipeableView::class.java.simpleName
    private var onChangeListener: OnChangeListener? = null
    private var yOffset: Float = 0f
    private var xOffset: Float = 0f
    private var heightAnimator: ValueAnimator? = null

    var onStateChange: ((state: DisplayOption) -> Unit)? = null
    var onHeightChange: ((height: Int) -> Unit)? = null

    var screenWidth: Int = 0
    var screenHeight: Int = 0

    var state: DisplayOption? = null

    var middleHeight: Int = 0
    var endHeight: Int = 0
    var startHeight: Int = 0

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeableView, 0, 0)
        middleHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeableView_middle_height, 0)
        endHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeableView_end_height, 0)
        startHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeableView_start_height, 0)
        typedArray.recycle()
        screenHeight = ScreenUtil(WeakReference(context)).getScreenHeight()
    }

    /**
     * To pass selected image
     */
    fun updateUi() {

    }

    fun changeState(option: DisplayOption) {
        changeState(option, -1)
    }

    fun changeState(option: DisplayOption, height: Int) {
        state = option
        when (option) {
            DisplayOption.START -> animateChangeHeight(startHeight)
            DisplayOption.MIDDLE -> animateChangeHeight(screenHeight / 2)
            DisplayOption.FULL -> animateChangeHeight(screenHeight)
            DisplayOption.SPECIFIC -> animateChangeHeight(height)
        }
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
        stopAllAnimation()
        yOffset = y
    }

    private fun onActionMove(x: Float, y: Float) {
        if (y < screenHeight - startHeight) return

        val params: ViewGroup.LayoutParams = layoutParams
        params.height = (screenHeight - y + yOffset).toInt()
        this.layoutParams = layoutParams

        initDisplayOption()
        onHeightChange?.let { it(height) }
    }

    private fun onActionUp(rawTouchX: Float, rawTouchY: Float) {
        val params: ViewGroup.LayoutParams = layoutParams
        if (params.height < screenHeight / 2 + screenHeight / 4) {
            animateChangeHeight(startHeight)
        } else if (params.height >= screenHeight / 2 + screenHeight / 4) {
            animateChangeHeight(screenHeight)
        }
    }

    private fun animateChangeHeight(to: Int, duration: Long = 300) {
        heightAnimator = ValueAnimator.ofInt(measuredHeight, to)
        heightAnimator?.addUpdateListener { valueAnimator ->
            val height: Int = valueAnimator.animatedValue as Int
            val layoutParams = layoutParams
            layoutParams.height = height
            this.layoutParams = layoutParams
            onHeightChange?.let { it(height) }
        }
        heightAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                initDisplayOption()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        heightAnimator?.duration = duration
        heightAnimator?.start()
    }

    private fun initDisplayOption() {
        when {
            height == startHeight -> DisplayOption.START
            height == screenHeight / 2 -> DisplayOption.MIDDLE
            else -> DisplayOption.FULL
        }
    }

    private fun stopAllAnimation() {
        animate().cancel()
        clearAnimation()
        heightAnimator?.cancel()
    }

    interface OnChangeListener {
        fun onViewCreated()

        fun onViewDismissed()

        fun onContentChanged()
    }

    enum class DisplayOption {
        /**
         * display view with [startHeight]
         */
        START,
        /**
         * display view with the middle screen width
         */
        MIDDLE,
        /**
         * display view with full screen height
         */
        FULL,
        /**
         * display view with specific height
         */
        SPECIFIC
    }

}