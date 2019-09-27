package com.anwesh.uiprojects.bitreeballview

/**
 * Created by anweshmishra on 27/09/19.
 */
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity

val levels : Int = 3
val scGap : Float = 0.05f
val strokeFactor : Float = 90f /(levels + 1)
val sizeFactor : Float = 3f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 45

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.gap() : Float {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    return Math.min(w, h) / (levels + 1)
}
fun Canvas.drawBallLine(i : Int, j : Int, scale : Float, paint : Paint) {
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = gap() / strokeFactor
    val size : Float = gap() / sizeFactor
    save()
    drawCircle(0f, 0f, size, paint)
    drawLine(0f, 0f, (i + 1) * gap(), 0f, paint)
    drawLine(0f, 0f, 0f, (i + 1) * gap(), paint)
    restore()
}

class BiTreeBallView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
}