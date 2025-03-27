package com.example.myweatherapp_kotlin

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast

class SwipeGestureListener(
    val context: Context,
    val sRightIntent: Intent?,
    val sLeftIntent: Intent?,
    val onDoubleTapCallback: () -> Unit
) : GestureDetector.SimpleOnGestureListener() {
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    @Override
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i("geocode", "Swipe detected")

        if (e1 == null) return false

        Log.i("geocode", "e1: $e1, e2: $e2, velocityX: $velocityX, velocityY: $velocityY")

        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y
        if (Math.abs(diffX) > Math.abs(diffY)) {  // Horizontal swipe
            if (Math.abs(diffX) > swipeThreshold && Math.abs(velocityX) > swipeVelocityThreshold) {
                if (diffX > 0) {
                    sRightIntent?.let { context.startActivity(it) }
                    Toast.makeText(context, "Swipe Right", Toast.LENGTH_SHORT).show();
                } else {
                    sLeftIntent?.let { context.startActivity(it) }
                    Toast.makeText(context, "Swipe Left", Toast.LENGTH_SHORT).show();
                }
                return true
            }
        }
        return false
    }

    @Override
    override fun onDoubleTap(e: MotionEvent): Boolean {
        Log.i("geocode", "Double Tap Detected")
        onDoubleTapCallback()
        return true
    }
}