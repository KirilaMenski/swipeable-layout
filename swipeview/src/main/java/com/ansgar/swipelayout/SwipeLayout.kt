package com.ansgar.swipelayout

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import java.lang.ref.WeakReference

class SwipeLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var yOffset: Float = 0f
    private var xOffset: Float = 0f
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var heightAnimator: ValueAnimator? = null

    var onStateChange: ((state: DisplayOption) -> Unit)? = null
    var onHeightChange: ((height: Int) -> Unit)? = null

    var state: DisplayOption? = null

    var middleHeight: Int = 0
    var endHeight: Int = 0
    var startHeight: Int = 0

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout, 0, 0)
        middleHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeLayout_middle_height, 0)
        endHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeLayout_end_height, 0)
        startHeight = typedArray.getDimensionPixelOffset(R.styleable.SwipeLayout_start_height, 0)
        typedArray.recycle()
        screenHeight = ScreenUtil(WeakReference(context)).getScreenHeight()
    }

    fun changeState(option: DisplayOption) {
        changeState(option, -1)
    }

    fun changeState(option: DisplayOption, height: Int) {
        state = option
        val position = when (option) {
            DisplayOption.START -> startHeight
            DisplayOption.MIDDLE -> middleHeight
            DisplayOption.FULL -> endHeight
            DisplayOption.SPECIFIC -> height
        }
        animateChangeHeight(position)
    }

    private var prevRawTouchY: Float = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val rawTouchX: Float = event.rawX
        val rawTouchY: Float = event.rawY
        val touchX: Float = event.x
        val touchY: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prevRawTouchY = rawTouchY
                yOffset = touchY
                onActionDown(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> onActionMove(rawTouchX, rawTouchY)
            MotionEvent.ACTION_UP -> onActionUp(rawTouchX, rawTouchY, prevRawTouchY > rawTouchY)
        }

        return true
    }

    private fun onActionDown(x: Float, y: Float) {
        stopAllAnimation()
    }

    private fun onActionMove(x: Float, y: Float) {
        val params: ViewGroup.LayoutParams = layoutParams
        params.height = (screenHeight - y + yOffset).toInt()
        this.layoutParams = layoutParams

        initDisplayOption()
        onHeightChange?.let { it(height) }
    }

    private fun onActionUp(rawTouchX: Float, rawTouchY: Float, directionTop: Boolean) {
        val top = screenHeight - middleHeight
        val position = when {
            rawTouchY < top && directionTop -> endHeight
            rawTouchY > top && directionTop -> middleHeight
            rawTouchY < top && !directionTop -> middleHeight
            rawTouchY > top && !directionTop -> startHeight
            else -> startHeight
        }
        animateChangeHeight(position)
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
                onStateChange?.let {
                    it(initDisplayOption())
                }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        heightAnimator?.duration = duration
        heightAnimator?.start()
    }

    private fun initDisplayOption(): DisplayOption = when {
        layoutParams.height == startHeight -> DisplayOption.START
        layoutParams.height == middleHeight -> DisplayOption.MIDDLE
        layoutParams.height == endHeight -> DisplayOption.FULL
        else -> DisplayOption.FULL
    }

    private fun stopAllAnimation() {
        animate().cancel()
        clearAnimation()
        heightAnimator?.cancel()
    }

    enum class DisplayOption {
        START,
        MIDDLE,
        FULL,
        SPECIFIC
    }

}