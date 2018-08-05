package com.ansgar.swipeableview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var i: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        swipeable_view.bottomIv = image_view
        swipeable_view.editText = edit_text
        swipeable_view.rightIVPosition = 500

        text_view.setOnClickListener {
            when (i) {
                0 -> swipeable_view.displayView(SwipeableView.DisplayOption.MIDDLE)
                1 -> swipeable_view.displayView(SwipeableView.DisplayOption.FULL)
                2 -> swipeable_view.displayView(SwipeableView.DisplayOption.START)
                3 -> swipeable_view.displayView(SwipeableView.DisplayOption.SPECIFIC, 720)
            }
            if (i == 3) i = 0
            else i++
        }
    }
}
