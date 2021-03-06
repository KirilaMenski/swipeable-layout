package com.ansgar.swipeableviewexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ansgar.memorymanager.MemoryManager
import com.ansgar.swipelayout.SwipeLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var screenHeight: Int = 0
    private var screenWidth: Int = 0
    private var mm: MemoryManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        edit_text.setOnClickListener {
//            edit_text.isFocusableInTouchMode = true
//            edit_text.isFocusable = true
//        }
//        expand_rl.setOnClickListener {
//            if (swipeable_view.state == SwipeLayout.DisplayOption.FULL) {
//                swipeable_view.changeState(SwipeLayout.DisplayOption.START)
//            } else {
//                swipeable_view.changeState(SwipeLayout.DisplayOption.FULL)
//            }
//        }

        main_activity.post {
            Log.i("MainScree n", "Height: ${main_activity.height}")
            swipeable_view.endHeight = main_activity.height
            screenHeight = main_activity.height
            screenWidth = main_activity.width
        }
        swipeable_view.onStateChange = { state ->
            when (state) {
                SwipeLayout.DisplayOption.START -> {
//                    edit_text.isFocusableInTouchMode = false
//                    edit_text.isFocusable = false
                }
            }
        }
        swipeable_view.onHeightChange = { height ->
            moveBottomIv(height)
            Log.i("Swipeable", "Height: $height")
        }
        swipeable_view.onStateChange = { state ->
            Log.i("Swipeable", "State: $state")
        }

        text_view.setOnClickListener {
            swipeable_view.changeState(SwipeLayout.DisplayOption.SPECIFIC, 640)
        }
    }

    override fun onStart() {
        super.onStart()
        mm = MemoryManager.init(this)
        mm?.x = 100
        mm?.y = 100
        mm?.delay = 1000
    }

    override fun onDestroy() {
        mm?.destroy()
        super.onDestroy()
    }

    private fun moveBottomIv(height: Int) {
        val viewPosPercent: Float = (height.toFloat() - swipeable_view.startHeight) / screenHeight
        val animatedIvPos: Float = screenWidth * viewPosPercent / 1.2f

        val way = image_view.left - 40
        if ((1 - viewPosPercent) > 0.83) {
            image_view.x = way * (1 - viewPosPercent * 6)
        } else {
            image_view.x = way * 0.05f
        }
        animateViews(animatedIvPos, height)
    }

    private fun animateViews(value: Float, height: Int) {
        val alpha = value / 1000

        val a: Float = (height.toFloat() / screenHeight / 2) * 400f
        if (height > screenHeight / 2) {
            if (expand_top_iv.rotation <= 315) {
                expand_top_iv.rotation = 135 + a
                expand_btm_iv.rotation = -45 + a
            } else {
                expand_top_iv.rotation = 315f
                expand_btm_iv.rotation = 135f
            }
        } else {
            expand_top_iv.rotation = 135f
            expand_btm_iv.rotation = -45f
        }
        Log.i("MainActivity", "rotation: ${expand_btm_iv.rotation}")
        header_container_rl.alpha = alpha * 2
    }

}
