package com.topwu.petsadopt.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.google.android.gms.maps.model.LatLng
import com.topwu.petsadopt.R
import com.topwu.petsadopt.util.GuiUtils

open class PulseMarkerView constructor(context: Context,
                                       latLng: LatLng,
                                       point: Point,
                                       position: Int = -1) : MarkerView(context, latLng, point) {

    val STROKE_DIMEN = 2

    val size: Float = GuiUtils.dpToPx(context, 32) / 2
    val scaleAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.pulse)
    val strokeBackgroundPaint = Paint()
    val backgroundPaint = Paint()
    val textPaint = TextPaint()
    val showAnimatorSet: AnimatorSet = AnimatorSet()
    val hideAnimatorSet: AnimatorSet = AnimatorSet()

    val text: String = if (position >= 0) Integer.toString(position) else ""

    init {
        visibility = INVISIBLE

        scaleAnimation.duration = 100

        backgroundPaint.color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
        backgroundPaint.isAntiAlias = true

        strokeBackgroundPaint.color = ContextCompat.getColor(context, android.R.color.white)
        strokeBackgroundPaint.style = Paint.Style.STROKE
        strokeBackgroundPaint.isAntiAlias = true
        strokeBackgroundPaint.strokeWidth = GuiUtils.dpToPx(context, STROKE_DIMEN)

        textPaint.color = ContextCompat.getColor(context, android.R.color.white)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = context.resources.getDimensionPixelSize(R.dimen.textsize_medium).toFloat()

        setupShowAnimatorSet()
        setupHideAnimatorSet()
    }

    private fun setupHideAnimatorSet() {
        val animatorScaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.0f, 0f)
        val animatorScaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.0f, 0f)
        val animator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f).setDuration(300)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                visibility = View.INVISIBLE
                invalidate()
            }
        })

        hideAnimatorSet.cancel()
        hideAnimatorSet.playTogether(animator, animatorScaleX, animatorScaleY)
    }

    private fun setupShowAnimatorSet() {
        val animatorScaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.5f, 1f)
        val animatorScaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.5f, 1f)
        val animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f).setDuration(300)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                visibility = View.VISIBLE
                invalidate()
            }
        })

        showAnimatorSet.cancel()
        showAnimatorSet.playTogether(animator, animatorScaleX, animatorScaleY)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        val frameParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                                   FrameLayout.LayoutParams.WRAP_CONTENT)
        frameParams.width = GuiUtils.dpToPx(context, 44).toInt()
        frameParams.height = GuiUtils.dpToPx(context, 44).toInt()
        frameParams.leftMargin = point.x - frameParams.width / 2
        frameParams.topMargin = point.y - frameParams.height / 2
        super.setLayoutParams(frameParams)
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        drawStrokeBackground(canvas)
        drawText(canvas)
        super.onDraw(canvas)
    }

    fun pulse() {
        startAnimation(scaleAnimation)
    }

    fun drawText(canvas: Canvas) {
        text.isNotEmpty().let {
            canvas.drawText(text, size, (size - ((textPaint.descent() + textPaint.ascent()) / 2)), textPaint)
        }
    }

    fun drawStrokeBackground(canvas: Canvas) {
        canvas.drawCircle(size, size, GuiUtils.dpToPx(context, 28) / 2, strokeBackgroundPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawCircle(size, size, size, backgroundPaint)
    }

    override fun show() {
        showAnimatorSet.start()
    }

    override fun hide() {
        hideAnimatorSet.start()
    }

    override fun refresh(point: Point) {
        this.point = point
        updatePulseViewLayoutParams(point)
    }

    fun showWithDelay(delay: Int) {
        showAnimatorSet.startDelay = delay.toLong()
        showAnimatorSet.start()
    }

    fun updatePulseViewLayoutParams(point: Point) {
        this.point = point
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                                              FrameLayout.LayoutParams.WRAP_CONTENT)
        params.width = GuiUtils.dpToPx(context, 44).toInt()
        params.height = GuiUtils.dpToPx(context, 44).toInt()
        params.leftMargin = point.x - params.width / 2
        params.topMargin = point.y - params.height / 2
        layoutParams = params

        invalidate()
    }
}