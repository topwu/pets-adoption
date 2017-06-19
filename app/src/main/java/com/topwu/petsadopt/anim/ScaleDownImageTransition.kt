package com.topwu.petsadopt.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView

class ScaleDownImageTransition constructor(private val context: Context,
                                           private var bitmap: Bitmap? = null) : Transition() {
    private val DEFAULT_SCALE_DOWN_FACTOR = 8
    private val PROPNAME_SCALE_X = "transitions:scale_down:scale_x"
    private val PROPNAME_SCALE_Y = "transitions:scale_down:scale_y"

    private var targetScaleFactor = DEFAULT_SCALE_DOWN_FACTOR

    init {
        if (bitmap == null) {
            interpolator = DecelerateInterpolator()
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    fun setScaleFactor(factor: Int) {
        targetScaleFactor = factor
    }

    override fun createAnimator(sceneRoot: ViewGroup,
                                startValues: TransitionValues?,
                                endValues: TransitionValues?): Animator? {
        if (startValues == null || null == endValues) {
            return null
        }
        val view = endValues.view
        if (view is ImageView) {
            if (bitmap != null) {
                view.setBackground(BitmapDrawable(context.resources, bitmap))
            }
            val scaleX = startValues.values[PROPNAME_SCALE_X] as Float
            val scaleY = startValues.values[PROPNAME_SCALE_Y] as Float

            val targetScaleX = endValues.values[PROPNAME_SCALE_X] as Float
            val targetScaleY = endValues.values[PROPNAME_SCALE_Y] as Float

            val scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, targetScaleX, scaleX)
            val scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, targetScaleY, scaleY)
            val parallelSet = AnimatorSet()
            parallelSet.playTogether(scaleXAnimator, scaleYAnimator, ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f))
            val sequentialSet = AnimatorSet()
            sequentialSet.playSequentially(parallelSet, ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f))
            return sequentialSet
        }
        return null
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, transitionValues.view.scaleX, transitionValues.view.scaleY)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, targetScaleFactor.toFloat(), targetScaleFactor.toFloat())
    }

    private fun captureValues(values: TransitionValues, scaleX: Float, scaleY: Float) {
        values.values.put(PROPNAME_SCALE_X, scaleX)
        values.values.put(PROPNAME_SCALE_Y, scaleY)
    }
}