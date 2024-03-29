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

val levels : Int = 5
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
fun Canvas.drawBallLine(i : Int, j : Int, x : Float, y : Float, scale : Float, paint : Paint) {
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = gap() / strokeFactor
    val size : Float = gap() / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    save()
    translate(x, y)
    paint.style = Paint.Style.STROKE
    drawCircle(0f, 0f, size, paint)
    paint.style = Paint.Style.FILL
    drawCircle(0f, 0f, size * sc1, paint)
    if (i < levels - 1) {
        drawLine(0f, 0f, gap() * sc2, 0f, paint)
    }
    if (j < levels - 1) {
        drawLine(0f, 0f, 0f,  gap() * sc2, paint)
    }
    restore()
}

class BiTreeBallView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class BTBNode(var i : Int, var j : Int, val state : State = State()) {

        private var right : BTBNode? = null
        private var down : BTBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < levels - 1) {
                right = BTBNode(i + 1, j)
            }
            if (j < levels - 1) {
                down = BTBNode(i, j + 1)
            }
        }

        fun draw(canvas : Canvas, x : Float, y : Float, paint : Paint) {
            canvas.drawBallLine(i, j, x, y, state.scale, paint)
            val gap : Float = canvas.gap()
            val sc : Float = state.scale.divideScale(1, 2)
            right?.draw(canvas, x + gap * sc, y, paint)
            down?.draw(canvas, x, y + gap * sc, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getRight() : BTBNode? = right

        fun getDown() : BTBNode? = down
    }

    data class BiBallTree(var i : Int) {

        private val root : BTBNode = BTBNode(0, 0)
        private val nodes : ArrayList<BTBNode> = ArrayList()
        private val childNodes : ArrayList<BTBNode> = ArrayList()
        init {
            nodes.add(root)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, canvas.gap(), canvas.gap(), paint)
        }

        fun update(cb : (Float) -> Unit) {
            val animNodes : ArrayList<BTBNode> = ArrayList()
            animNodes.addAll(nodes)
            animNodes.forEach { node ->
                node.update {
                    nodes.remove(node)
                    val right : BTBNode? = node.getRight()
                    val down : BTBNode? = node.getDown()
                    if (right != null) {
                        childNodes.add(right)
                    }
                    if (down != null) {
                        childNodes.add(down)
                    }
                    if (nodes.size == 0) {
                        cb(it)
                        nodes.addAll(childNodes)
                        childNodes.clear()
                    }
                }
            }
        }

        fun startUpdating(cb : () -> Unit) {
            var k : Int = 0
            nodes.forEach { node ->
                node.startUpdating {
                    k++
                    if (k == nodes.size) {
                        cb()
                    }
                }
            }
        }
    }

    data class Renderer(var view : BiTreeBallView) {

        private val animator : Animator = Animator(view)
        private val bbt : BiBallTree = BiBallTree(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bbt.draw(canvas, paint)
            animator.animate {
                bbt.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bbt.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BiTreeBallView {
            val view : BiTreeBallView = BiTreeBallView(activity)
            activity.setContentView(view)
            return view
        }
    }
}