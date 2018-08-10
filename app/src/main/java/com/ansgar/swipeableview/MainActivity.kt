package com.ansgar.swipeableview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var i: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        swipeable_view.bottomIv = image_view
        swipeable_view.editText = edit_text
        swipeable_view.expandBottomArrow = expand_btm_iv
        swipeable_view.expandTopArrow = expand_top_iv
//        edit_text.clearFocus()
//        edit_text.isFocusableInTouchMode = false
//        edit_text.isFocusable = false
        edit_text.setOnClickListener {
            edit_text.isFocusableInTouchMode = true
            edit_text.isFocusable = true
        }
        expand_rl.setOnClickListener {
            if (swipeable_view.state == SwipeableView.DisplayOption.FULL) {
                swipeable_view.changeState(SwipeableView.DisplayOption.START)
            } else {
                swipeable_view.changeState(SwipeableView.DisplayOption.FULL)
            }
        }
        swipeable_view.onStateChange = { state ->
            when (state) {
                SwipeableView.DisplayOption.START -> {
//                    swipeable_view.changeState(SwipeableView.DisplayOption.MIDDLE)
                    edit_text.isFocusableInTouchMode = false
                    edit_text.isFocusable = false
                }
            }
        }
        swipeable_view.onHeightChange = {height -> Log.i("Swipeable", "Height: $height")}
        swipeable_view.endIvPosition = 0.95
        swipeable_view.startIvPos = 40

        text_view.setOnClickListener {
            when (i) {
                0 -> swipeable_view.changeState(SwipeableView.DisplayOption.MIDDLE)
                1 -> swipeable_view.changeState(SwipeableView.DisplayOption.FULL)
                2 -> swipeable_view.changeState(SwipeableView.DisplayOption.START)
                3 -> swipeable_view.changeState(SwipeableView.DisplayOption.SPECIFIC, 720)
            }
            if (i == 3) i = 0
            else i++
        }
    }
}
