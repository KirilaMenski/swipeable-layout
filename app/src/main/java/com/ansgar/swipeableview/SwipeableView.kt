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

/**
 * Copyright (c) 2018 SwipeableView. All rights reserved.
 */
class SwipeableView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val TAG: String = SwipeableView::class.java.simpleName
    private var onChangeListener: OnChangeListener? = null
    private var yOffset: Float = 0f
    private var xOffset: Float = 0f
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var heightAnimator: ValueAnimator? = null
    private var animatedIvResId: Int = -1
    private var contentResId: Int = -1
    private var headerResId: Int = -1
    private var bottomResId: Int = -1
    private var leftIVPosition: Int = 0

    var onStateChange: ((state: DisplayOption) -> Unit)? = null
    var onHeightChange:((height: Int) -> Unit)? = null

    var state: DisplayOption? = null
        get() {
            return when {
                height == startHeight -> DisplayOption.START
                height == getScreenHeight() / 2 -> DisplayOption.MIDDLE
                else -> DisplayOption.FULL
            }
        }

    var startIvPos: Int = 0
    var endIvPosition: Double = 1.0
    var middleHeight: Int = 0
    var endHeight: Int = 0
    var startHeight: Int = 0

    var bottomIv: ImageView? = null
    var editText: EditText? = null
    var contentContainer: ViewGroup? = null
    var headContainer: ViewGroup? = null
    var bottomContainer: ViewGroup? = null
    var expandTopArrow: View? = null
    var expandBottomArrow: View? = null

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeableView, 0, 0)
        animatedIvResId = typedArray.getResourceId(R.styleable.SwipeableView_image_view_id, -1)
        contentResId = typedArray.getResourceId(R.styleable.SwipeableView_content_container, -1)
        headerResId = typedArray.getResourceId(R.styleable.SwipeableView_head_container, -1)
        bottomResId = typedArray.getResourceId(R.styleable.SwipeableView_bottom_container, -1)
        middleHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeableView_middle_height, 0)
        endHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeableView_end_height, 0)
        startHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeableView_start_height, 0)
        typedArray.recycle()
        screenWidth = getScreenWidth()
        screenHeight = getScreenHeight()

        initViews()
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
            DisplayOption.MIDDLE -> animateChangeHeight(getScreenHeight() / 2)
            DisplayOption.FULL -> animateChangeHeight(getScreenHeight())
            DisplayOption.SPECIFIC -> animateChangeHeight(height)
        }
    }

    private fun initViews() {
        this.post {
            bottomIv = findViewById(animatedIvResId)
            contentContainer = findViewById(contentResId)
            headContainer = findViewById(headerResId)
            bottomContainer = findViewById(bottomResId)
            leftIVPosition = bottomIv?.left ?: 0
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
        val params: ViewGroup.LayoutParams = layoutParams
        params.height = (screenHeight - y + yOffset).toInt()
        this.layoutParams = layoutParams

        onHeightChange?.let { it(height) }
        moveBottomIv()
    }

    private fun onActionUp(rawTouchX: Float, rawTouchY: Float) {
        val params: ViewGroup.LayoutParams = layoutParams
        if (params.height < getScreenHeight() / 2 + getScreenHeight() / 4) {
            animateChangeHeight(startHeight)
        } else if (params.height >= getScreenHeight() / 2 + getScreenHeight() / 4) {
            animateChangeHeight(getScreenHeight())
        }
    }

    private fun moveBottomIv() {
        val viewPosPercent: Float = (height.toFloat() - startHeight) / (getScreenHeight())
        val animatedIvPos: Float = width * viewPosPercent / 1.2f

        val way = bottomIv?.left ?: 0-startIvPos
        if ((1 - viewPosPercent) > 0.83) {
            bottomIv?.x = way * (1 - viewPosPercent * 6)
        } else {
            bottomIv?.x = way * 0.05f
        }
        animateViews(animatedIvPos)
    }

    private fun animateViews(value: Float) {
        val alpha = value / 1000
        if (height < startHeight * 2) {
            contentContainer?.visibility = View.GONE
            headContainer?.visibility = View.GONE
        } else {
            contentContainer?.visibility = View.VISIBLE
            headContainer?.visibility = View.VISIBLE
        }

        val a: Float = (height.toFloat() / getScreenHeight() / 2) * 400f
        if (height > getScreenHeight() / 2) {
            if (expandTopArrow?.rotation!! <= 315) {
                expandTopArrow?.rotation = 135 + a
                expandBottomArrow?.rotation = -45 + a
            } else {
                expandTopArrow?.rotation = 315f
                expandBottomArrow?.rotation = 135f
            }
        } else {
            expandTopArrow?.rotation = 135f
            expandBottomArrow?.rotation = -45f
        }
        Log.i(TAG, "rotation: ${expandBottomArrow?.rotation}")
        contentContainer?.alpha = alpha * 2
        headContainer?.alpha = alpha * 2
    }

    private fun animateChangeHeight(to: Int, duration: Long = 300) {
        heightAnimator = ValueAnimator.ofInt(measuredHeight, to)
        heightAnimator?.addUpdateListener { valueAnimator ->
            val height: Int = valueAnimator.animatedValue as Int
            val layoutParams = layoutParams
            layoutParams.height = height
            this.layoutParams = layoutParams
            moveBottomIv()
        }
        heightAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                onHeightChange?.let { it(height) }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        heightAnimator?.duration = duration
        heightAnimator?.start()
    }

    private fun stopAllAnimation() {
        animate().cancel()
        clearAnimation()
        heightAnimator?.cancel()
    }

    private fun getDisplayMetric(): DisplayMetrics {
        val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        return displayMetrics
    }

    private fun getScreenHeight(): Int {
        return getDisplayMetric().heightPixels
    }

    private fun getScreenWidth(): Int {
        return getDisplayMetric().widthPixels
    }

    private fun convertToPixels(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dp, getDisplayMetric())

    private fun convertToDp(px: Int): Float = px / getDisplayMetric().density

    private fun getDensity(): Float = getDisplayMetric().density

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