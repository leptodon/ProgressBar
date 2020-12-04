package ru.cactus.progressbar.animation

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import ru.cactus.progressbar.R
import kotlin.properties.Delegates

class CustomProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mBodyStrokeWidthPx by Delegates.notNull<Float>()
    private var mGlowStrokeWidthPx by Delegates.notNull<Float>()
    private var mPaddingPx by Delegates.notNull<Float>()

    private var mPaintBody: Paint = Paint()
    private var mPaintBackground: Paint = Paint()
    private var mPaintOrangeBackground: Paint = Paint()
    private var mPaintGlow: Paint = Paint()
    private var mRect: RectF
    private lateinit var mBodyGradient: SweepGradient
    private lateinit var mBodyGradientFromToColors: IntArray
    private lateinit var mGradientFromToPositions: FloatArray
    private var currentAngle by Delegates.notNull<Float>()
    private lateinit var animatorSet: AnimatorSet

    private var currentColor by Delegates.notNull<Int>()

    private val backgroundColor: Int =
        ResourcesCompat.getColor(resources, R.color.background_color, null)
    private val blueColor: Int = ResourcesCompat.getColor(resources, R.color.blue, null)
    private val orangeColor: Int = ResourcesCompat.getColor(resources, R.color.orange, null)

    private var drawState: Int = 0


    companion object {
        private const val BODY_STROKE_WIDTH: Int = 6
        private const val GLOW_STROKE_WIDTH: Int = 6
        private const val PADDING: Int = GLOW_STROKE_WIDTH / 2 + BODY_STROKE_WIDTH
        private const val BODY_LENGTH: Float = 240f
        private const val NORMALIZED_GRADIENT_LENGTH: Float = BODY_LENGTH / 480f
        private const val ROTATION_LENGTH: Float = 360f
        private const val ROTATION_DURATION: Long = 2000
        private const val TAIL_GROW_DURATION: Long = 4000
        private const val ROTATION_PROPERTY_NAME: String = "rotation"
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        val displayMetrics: DisplayMetrics = resources.displayMetrics

        currentAngle = 10f

        mBodyStrokeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            BODY_STROKE_WIDTH.toFloat(),
            displayMetrics
        )
        mPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            PADDING.toFloat(),
            displayMetrics
        )
        mGlowStrokeWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            GLOW_STROKE_WIDTH.toFloat(),
            displayMetrics
        )

        mRect = RectF()
        drawPaint(backgroundColor, blueColor)
    }

    fun handlerState(state: EngineState) {
        when (state) {
            EngineState.START -> cleanStart()
            EngineState.START_SUCCESS -> changeColor()
            EngineState.START_TIMEOUT -> changeColorAndStop()
            EngineState.STOP -> TODO()
            EngineState.STOP_SUCCESS -> TODO()
            EngineState.STOP_TIMEOUT -> TODO()
        }
    }

    private fun growTail() {
        Thread(Runnable {
            kotlin.run {
                for (i in 30..BODY_LENGTH.toInt()) {
                    currentAngle = i.toFloat()
                    try {
                        Thread.sleep(16)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }).start()
    }

    override fun onDraw(canvas: Canvas?) {
        val left: Float = mPaddingPx
        val top: Float = mPaddingPx
        val right: Float = width - mPaddingPx
        val bottom: Float = height - mPaddingPx
        mRect.set(left, top, right, bottom)
        canvas?.rotate(rotation, (width / 2).toFloat(), (height / 2).toFloat())
        when (drawState) {
            0 -> {
                canvas?.drawArc(mRect, 0f, 360f, false, mPaintBackground)
                canvas?.drawArc(mRect, 10f, currentAngle, false, mPaintBody)
                canvas?.drawArc(mRect, currentAngle + 10f, -10f, false, mPaintGlow)
            }
            1 -> {
                canvas?.drawArc(mRect, 0f, 360f, false, mPaintBackground)
            }
        }
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX: Float = (width / 2).toFloat()
        val centerY: Float = (height / 2).toFloat()
        mBodyGradient =
            SweepGradient(centerX, centerY, mBodyGradientFromToColors, mGradientFromToPositions)
        mPaintBody.shader = mBodyGradient
//        mPaintGlow.shader = mBodyGradient
    }


    private fun createRotationAnimator(): ObjectAnimator {
        val rotateAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(this, ROTATION_PROPERTY_NAME, ROTATION_LENGTH)
        rotateAnimator.duration = ROTATION_DURATION
        rotateAnimator.interpolator = LinearInterpolator()
        rotateAnimator.repeatMode = ValueAnimator.RESTART
        rotateAnimator.repeatCount = ValueAnimator.INFINITE
        return rotateAnimator
    }

    private fun createGrowTailAnimator(): ValueAnimator {
        val growAnimator: ValueAnimator = ValueAnimator.ofFloat(currentAngle, currentAngle + 270f)
        growAnimator.addUpdateListener {
            ValueAnimator.AnimatorUpdateListener { animation ->
                currentAngle = animation.animatedValue as Float
            }
        }
        growAnimator.duration = TAIL_GROW_DURATION
        growAnimator.repeatCount = 1
        growAnimator.interpolator = LinearInterpolator()
        return growAnimator
    }

    private fun cleanStart() {
        animatorSet = AnimatorSet()
        animatorSet.play(createGrowTailAnimator())
            .after(createChangeColorAnimator()).after(createRotationAnimator())
//        animatorSet.playTogether(createGrowTailAnimator(), createRotationAnimator())
        animatorSet.duration = 4000
        animatorSet.start()
        growTail()
    }

    private fun createChangeColorAnimator(): ValueAnimator {
        val transitionColor: ValueAnimator = ValueAnimator.ofInt(
            blueColor,
            orangeColor
        )
        transitionColor.setEvaluator(ArgbEvaluator())
        transitionColor.duration = 5
        transitionColor.addUpdateListener {
            ValueAnimator.AnimatorUpdateListener { animation ->
                val newcolor: Int = animation.animatedValue as Int
                drawPaint(backgroundColor, newcolor)
            }
        }
        return transitionColor
    }

    private fun drawPaint(colorBack: Int, colorCircle: Int) {

        mPaintBackground.color = colorBack
        mPaintBackground.strokeWidth = mBodyStrokeWidthPx
        mPaintBackground.style = Paint.Style.STROKE

//        mPaintOrangeBackground.color = orangeColor
//        mPaintOrangeBackground.strokeWidth = mBodyStrokeWidthPx
//        mPaintOrangeBackground.style = Paint.Style.STROKE


        mPaintBody.isAntiAlias = true
        mPaintBody.color = colorCircle
        mPaintBody.strokeWidth = mBodyStrokeWidthPx
        mPaintBody.style = Paint.Style.STROKE
        mPaintBody.strokeJoin = Paint.Join.ROUND
        mPaintBody.strokeCap = Paint.Cap.ROUND

        mPaintGlow.set(mPaintBody)
        mPaintGlow.color = colorCircle
        mPaintGlow.strokeWidth = mGlowStrokeWidthPx
        mPaintGlow.alpha = 200
        mPaintGlow.maskFilter = BlurMaskFilter(mBodyStrokeWidthPx, BlurMaskFilter.Blur.OUTER)

        mBodyGradientFromToColors = intArrayOf(Color.TRANSPARENT, colorCircle)
        mGradientFromToPositions = floatArrayOf(0F, NORMALIZED_GRADIENT_LENGTH)

        invalidate()
    }

    private fun changeColor() {
        createChangeColorAnimator().start()
//        mPaintBody.color = orangeColor
//        mPaintGlow.color = orangeColor
////        val fadeColor: ObjectAnimator = ObjectAnimator.ofObject(mPaintBody, "color", ArgbEvaluator(), currentColor ) as ObjectAnimator
////        fadeColor.duration = 5
////        fadeColor.interpolator = LinearInterpolator()
////        fadeColor.addUpdateListener { ValueAnimator.AnimatorUpdateListener {
////            animation -> currentColor = animation.animatedValue as Int
////            Log.d("TAG", animation.animatedValue.toString())
////            invalidate()
////        } }
////        fadeColor.start()
////        currentColor = orangeColor
//        mBodyGradientFromToColors = intArrayOf(Color.TRANSPARENT, orangeColor)
//        onSizeChanged(width, height, width, height)
    }

    private fun changeColorAndStop() {
        createChangeColorAnimator().start()
//        mPaintBackground.color = orangeColor
//        drawState = 1
//        invalidate()
    }
}

enum class EngineState {
    START,
    START_SUCCESS,
    START_TIMEOUT,
    STOP,
    STOP_SUCCESS,
    STOP_TIMEOUT
}