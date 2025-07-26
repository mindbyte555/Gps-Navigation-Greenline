package com.example.gpstest.utls

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController

object GradientAnimator {

    private val gradientColorsList = listOf(
        intArrayOf(0xFF360033.toInt(), 0xFF0B8793.toInt()),
        intArrayOf(0xFF6441A5.toInt(), 0xFFDC2430.toInt()),
        intArrayOf(0xFFC779D0.toInt(), 0xFF4BC0C8.toInt()),
        intArrayOf(0xFF185A9D.toInt(), 0xFF43CEA2.toInt()),
        intArrayOf(0xFF780206.toInt(), 0xFF061161.toInt())
    )

    private const val duration = 3000L

    fun start(view: View, activity: Activity? = null, startIndex: Int = 0) {
        animateGradient(view, activity, startIndex)
    }

    private fun animateGradient(view: View, activity: Activity?, gradientIndex: Int) {
        val evaluator = ArgbEvaluator()
        val currentColors = gradientColorsList[gradientIndex % gradientColorsList.size]
        val nextColors = gradientColorsList[(gradientIndex + 1) % gradientColorsList.size]

        val colorAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = GradientAnimator.duration
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                val startColor =
                    evaluator.evaluate(fraction, currentColors[0], nextColors[0]) as Int
                val endColor = evaluator.evaluate(fraction, currentColors[1], nextColors[1]) as Int

                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(startColor, endColor)
                )
                gradient.cornerRadius = 0f
                view.background = gradient

                // Also update status and navigation bar
                activity?.let {
                    setSystemBarsColor(it.window, startColor)
                }
            }
            doOnEnd {
                animateGradient(view, activity, gradientIndex + 1)
            }
            start()
        }
    }

    private fun setSystemBarsColor(window: Window, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
            window.navigationBarColor = color

            // Optional: change icon color for light/dark contrast
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = window.insetsController
                controller?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }
        }
    }

    private fun ValueAnimator.doOnEnd(action: () -> Unit) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationEnd(animation: android.animation.Animator) = action()
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }
}