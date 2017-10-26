package org.liaohailong.pdftestapp.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 手指拽动控件
 * Created by LHL on 2017/10/26.
 */
class DragView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {

    private var paint: Paint? = null
    private var mWidth = 0
    private var mHeight = 0
    private var mRadius = 0f

    private var lastX = 0f
    private var lastY = 0f

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null)

    init {
        paint = Paint()
        paint?.style = Paint.Style.FILL_AND_STROKE
        paint?.isAntiAlias = true
        paint?.color = Color.RED

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_UP or MotionEvent.ACTION_CANCEL -> {
                    lastX = mWidth / 2f
                    lastY = mHeight / 2f
                    invalidate()
                }
            }
            true
        }
    }

    public override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w
        mHeight = h
        lastX = w / 2f
        lastY = h / 2f
        mRadius = if (w > h) h / 5f else w / 5f
    }

    public override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(lastX, lastY, mRadius, paint)
    }


}
