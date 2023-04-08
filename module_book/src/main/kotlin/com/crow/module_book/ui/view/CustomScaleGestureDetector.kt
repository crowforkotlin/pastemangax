package com.crow.module_book.ui.view

import android.content.Context
import android.view.ScaleGestureDetector

class CustomScaleGestureDetector(val mRv: CustomRecyclerView, val mContext: Context) : ScaleGestureDetector(mContext, object : OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }
    })