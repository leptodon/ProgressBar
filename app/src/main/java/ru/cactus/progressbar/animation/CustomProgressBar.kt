package ru.cactus.progressbar.animation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
    private var mBodyGradientFromToColors: IntArray
    private var mGradientFromToPositions: FloatArray
    private var currentAngle: Float = 0.0f
    private lateinit var animatorSet: AnimatorSet
    private var drawState: DrawState = DrawState.BACKGROUND


    private var currentColor by Delegates.notNull<Int>()

    private val greyColor: Int = ResourcesCompat.getColor(resources, R.color.grey, null)
    private val baseColor: Int = ResourcesCompat.getColor(resources, R.color.blue, null)
    private val altColor: Int = ResourcesCompat.getColor(resources, R.color.orange, null)

    private lateinit var currentState:EngineState

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

        mPaintBackground.color = greyColor
        mPaintBackground.strokeWidth = mBodyStrokeWidthPx
        mPaintBackground.style = Paint.Style.STROKE

        currentColor = baseColor

        mRect = RectF()

        mPaintBody.isAntiAlias = true
        mPaintBody.color = baseColor
        mPaintBody.strokeWidth = mBodyStrokeWidthPx
        mPaintBody.style = Paint.Style.STROKE
        mPaintBody.strokeJoin = Paint.Join.ROUND
        mPaintBody.strokeCap = Paint.Cap.ROUND

        mPaintGlow.set(mPaintBody)
        mPaintGlow.color = baseColor
        mPaintGlow.strokeWidth = mGlowStrokeWidthPx
        mPaintGlow.alpha = 200
        mPaintGlow.maskFilter = BlurMaskFilter(mBodyStrokeWidthPx, BlurMaskFilter.Blur.OUTER)

        mBodyGradientFromToColors = intArrayOf(Color.TRANSPARENT, baseColor)
        mGradientFromToPositions = floatArrayOf(0F, NORMALIZED_GRADIENT_LENGTH)

        animatorSet = AnimatorSet()
    }

    fun handlerState(state: EngineState) {
        when (state) {
            EngineState.START -> {
                currentState = EngineState.START
                playStartAction()
            }
            EngineState.START_SUCCESS -> {
                currentState = EngineState.START_SUCCESS
                playStartSuccessAction()
            }
            EngineState.START_TIMEOUT -> {
                currentState = EngineState.START_TIMEOUT
                playStartTimeoutAction()
            }
            EngineState.STOP -> {
                currentState = EngineState.STOP
                playStopAction()
            }
            EngineState.STOP_SUCCESS -> {
                currentState = EngineState.STOP_SUCCESS
                playStopSuccessAction()
            }
            EngineState.STOP_TIMEOUT -> {
                currentState = EngineState.STOP_TIMEOUT
                playStopTimeoutAction()
            }
        }
    }

    private fun growTail() {
        Thread(Runnable {
            kotlin.run {
                for (i in 30..BODY_LENGTH.toInt()) {
                    currentAngle = i.toFloat()
                    Log.d("TAG", "GROW_TAIL_COUNTER")
                    try {
                        Thread.sleep(16)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                when (currentState) {
                    EngineState.START ->  changeColorTo(altColor)
                    EngineState.STOP -> changeColorTo(baseColor)
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
            DrawState.ACTION -> {
                canvas?.drawArc(mRect, 0f, 360f, false, mPaintBackground)
                canvas?.drawArc(mRect, 10f, currentAngle, false, mPaintBody)
                canvas?.drawArc(mRect, currentAngle + 10f, -10f, false, mPaintGlow)
            }
            DrawState.BACKGROUND -> {
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

    private fun playStartAction() {
        Log.d("currentAngle", "current angle = " + currentAngle)
        if (!animatorSet.isRunning) {
            if (currentColor != baseColor) {
                changeColorTo(baseColor)
            }
            drawState = DrawState.ACTION
            mPaintBackground.color = greyColor
            animatorSet.playTogether(createGrowTailAnimator(), createRotationAnimator())
            animatorSet.start()
            growTail()
        }
    }

    private fun playStopAction() {
        if (!animatorSet.isRunning) {
            if (currentColor != altColor) {
                changeColorTo(altColor)
            }
            drawState = DrawState.ACTION
            mPaintBackground.color = greyColor
            animatorSet.playTogether(createGrowTailAnimator(), createRotationAnimator())
            animatorSet.start()
            growTail()
        }
    }

    private fun playStartSuccessAction() {
        mPaintBackground.color = altColor
        drawState = DrawState.BACKGROUND
        animatorSet.cancel()
        animatorSet.removeAllListeners()
    }

    private fun playStopSuccessAction() {
        mPaintBackground.color = baseColor
        drawState = DrawState.BACKGROUND
        animatorSet.cancel()
        animatorSet.removeAllListeners()
    }

    private fun playStartTimeoutAction() {
        mPaintBackground.color = baseColor
        drawState = DrawState.BACKGROUND
        animatorSet.cancel()
        animatorSet.removeAllListeners()
    }

    private fun playStopTimeoutAction() {
        mPaintBackground.color = altColor
        drawState = DrawState.BACKGROUND
        animatorSet.cancel()
        animatorSet.removeAllListeners()
    }

    private fun changeColorTo(color: Int) {
        when (color) {
            baseColor -> {
                currentColor = baseColor
                mPaintBody.color = baseColor
                mPaintGlow.color = baseColor
                mBodyGradientFromToColors = intArrayOf(Color.TRANSPARENT, baseColor)
            }
            altColor -> {
                currentColor = altColor
                mPaintBody.color = altColor
                mPaintGlow.color = altColor
                mBodyGradientFromToColors = intArrayOf(Color.TRANSPARENT, altColor)
            }
        }
        onSizeChanged(width, height, width, height)
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

enum class DrawState {
    BACKGROUND,
    ACTION
}