package com.hassiel.rideadvisor.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * Vista personalizada que dibuja la animación de radar de la pantalla
 * "Radar IA" (sección 8). Ligera y pausable para no gastar batería
 * innecesariamente cuando la pantalla no está visible (sección 21).
 */
class RadarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#2A2F3A")
    }

    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#00E38A")
    }

    private var sweepAngle = 0f
    private var animator: ValueAnimator? = null
    private var isPulsing = false

    fun start() {
        if (animator?.isRunning == true) return
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            interpolator = android.view.animation.LinearInterpolator()
            addUpdateListener {
                sweepAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stop() {
        animator?.cancel()
        animator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) / 2f - 8f
        if (radius <= 0f) return

        // Anillos concéntricos.
        for (i in 1..3) {
            canvas.drawCircle(cx, cy, radius * i / 3f, ringPaint)
        }

        // Gradiente en forma de "barrido" que gira.
        canvas.save()
        canvas.rotate(sweepAngle, cx, cy)
        val gradient = SweepGradientCompat.create(cx, cy, radius)
        sweepPaint.shader = gradient
        canvas.drawCircle(cx, cy, radius, sweepPaint)
        canvas.restore()

        // Punto central.
        canvas.drawCircle(cx, cy, 6f, dotPaint)
    }

    private object SweepGradientCompat {
        fun create(cx: Float, cy: Float, radius: Float): Shader {
            return android.graphics.SweepGradient(
                cx, cy,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    Color.parseColor("#3300E38A"),
                    Color.parseColor("#8800E38A")
                ),
                floatArrayOf(0f, 0.7f, 0.9f, 1f)
            )
        }
    }
}
