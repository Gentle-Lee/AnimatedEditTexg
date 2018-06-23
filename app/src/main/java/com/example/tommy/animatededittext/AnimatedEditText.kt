package com.example.tommy.animatededittext

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import android.content.ContentValues.TAG





class AnimatedEditText(context : Context, attrs: AttributeSet) : EditText(context, attrs) {
    private val TAG:String = "AnimatedEditText"

    private var hintTextSize = 0f
    private var hintTextColor = 0f
    private var hintText:String = "搜索"
    private var hintWidth = 0f
    private var hintHeight = 0f


    private var imageWidth = 0f
    private var mDrawable:Drawable? = null
    private var imgInable:Drawable? = null

    private var drawableStartX:Float = 0.toFloat()
    private var drawableEndX :Float = 0.toFloat()
    private var drawableStartY:Float = 0.toFloat()
    private var drawableEndY:Float = 0.toFloat()
    private var hintStartX:Float = 0.toFloat()
    private var hintEndX:Float = 0.toFloat()
    private var hintStartY:Float = 0.toFloat()
    private var hintEndY:Float = 0.toFloat()

    private var paint:Paint? = null
    private var mCanvas:Canvas? = null

    private var focus:Boolean = false
    private var mStartTime:Int = -1
    private val mStartOffset:Int = 1
    private val mDuration:Int = 200

    init {
        initResource(context,attrs)
        initPaint()
        addDeleteButtonListener()
    }

    private fun initResource(context: Context,attrs: AttributeSet){
        val mTypeArray:TypedArray =  context.obtainStyledAttributes(attrs, R.styleable.AnimatedEditText)
        val density = context.resources.displayMetrics.density
        val drawableId = mTypeArray.getInt(R.styleable.AnimatedEditText_imageSrc,R.drawable.icon_search)
        imageWidth = mTypeArray.getDimension(R.styleable.AnimatedEditText_imageWidth,18 * density + 0.5f)
        hintTextColor = mTypeArray.getColor(R.styleable.AnimatedEditText_hintTextColor,-0x7b7b7c).toFloat()
        hintTextSize = mTypeArray.getDimension(R.styleable.AnimatedEditText_hintTextSize,14 * density + 0.5f)
        hintText = mTypeArray.getString(R.styleable.AnimatedEditText_hintText)?:"搜索"
        mDrawable = ResourcesCompat.getDrawable(resources, drawableId, null)
        mDrawable?.setBounds(0,0,imageWidth.toInt(),imageWidth.toInt())
        mTypeArray.recycle()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        focus = focused
    }
    private fun initPaint(){
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = hintTextColor.toInt()
        paint!!.textSize = hintTextSize
        hintWidth = paint!!.measureText(hintText)
        val fm = paint!!.fontMetrics
        hintHeight = fm.bottom - fm.top
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.i(TAG, "onMeasure: ")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.i(TAG, "onLayout: ")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mCanvas = canvas
        if (!focus){
            drawCenterBackGround(mCanvas)
            mStartTime = -1
        }else{
            animatedFocus()
        }
    }

    private fun drawCenterBackGround(canvas: Canvas?){
        if (this.text.toString().isEmpty() && !focus){
            val dx = (width.toFloat() - imageWidth - hintWidth - 8f) / 2
            val dy = (height - imageWidth) / 2
            canvas?.save()
            canvas?.translate(scrollX + dx, scrollY + dy)
            if (mDrawable != null) {
                mDrawable!!.draw(canvas)
            }
            canvas?.drawText(
                    hintText,
                    scrollX.toFloat() + imageWidth + 8f,
                    scrollY + (height - (height - hintHeight) / 2) - paint!!.fontMetrics.bottom - dy,
                    paint!!)
            canvas?.restore()
        }
    }

    private fun animatedFocus(){
        var done = false
        if (mStartTime == -1) {
            mStartTime = SystemClock.uptimeMillis().toInt()
        }
        val curTime = SystemClock.uptimeMillis()
        // t为一个0到1均匀变化的值
        var t = (curTime - mStartTime - mStartOffset) / mDuration.toFloat()
        t = Math.max(0f, Math.min(t, 1f))
        val dx = (width.toFloat() - imageWidth - hintWidth - 8f) / 2
        val dy = (height - imageWidth) / 2
        drawableStartX = (width.toFloat() - imageWidth - hintWidth - 8f) / 2
        drawableEndX = imageWidth*-1
        drawableStartY = scrollY + dy
        drawableEndY = drawableStartY

        hintStartX = scrollX.toFloat() + imageWidth + 8f
        hintEndX = width.toFloat()+imageWidth
        hintEndY = scrollY + (height - (height - hintHeight) / 2) - paint!!.fontMetrics.bottom - dy
        hintStartY = hintEndY
        val translateX = lerp(drawableStartX, drawableEndX, t).toInt()
        val translateY = lerp(drawableStartY, drawableEndY, t).toInt()
        val fontTranslateX = lerp(hintStartX, hintEndX, t).toInt()
        val fontTranslateY = lerp(hintStartY, hintEndY, t).toInt()
        if (t < 1) {
            done = false
        }
        if (0 < t && t <= 1) {
            done = false
            // 保存画布，方便下次绘制
            mCanvas?.save()
            mCanvas?.translate(translateX.toFloat(), translateY.toFloat())
            mDrawable!!.draw(mCanvas)
            mCanvas?.drawText(hintText, fontTranslateX.toFloat(), fontTranslateY.toFloat(), paint!!)
            mCanvas?.restore()
        }
        if (!done) {
            invalidate()
        }
    }
    internal fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    private fun addDeleteButtonListener(){
        imgInable = ResourcesCompat.getDrawable(resources, R.drawable.delete, null)
        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                setDrawable()
            }
        })
    }

    // 设置删除图片
    private fun setDrawable() {
        if (length() < 1)
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        else
            setCompoundDrawablesWithIntrinsicBounds(null, null, imgInable, null)
    }

    // 处理删除事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (imgInable != null && event.action == MotionEvent.ACTION_UP) {
            val eventX = event.rawX.toInt()
            val eventY = event.rawY.toInt()
            Log.e(TAG, "eventX = $eventX; eventY = $eventY")
            val rect = Rect()
            getGlobalVisibleRect(rect)
            rect.left = rect.right - 100
            if (rect.contains(eventX, eventY))
                setText("")
        }
        return super.onTouchEvent(event)
    }



    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i(TAG,"onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        //    if (mDrawable != null) {
        //      mDrawable.setCallback(null);
        //      mDrawable = null;
        //    }
        super.onDetachedFromWindow()
        Log.i(TAG,"onDetachedFromWindow")

    }

}