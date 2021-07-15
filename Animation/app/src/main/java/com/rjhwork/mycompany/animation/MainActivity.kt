package com.rjhwork.mycompany.animation

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: View
    private lateinit var sunView:View
    private lateinit var skyView:View

    private var flag = true

    private val blueSkyColor:Int by lazy {
        ContextCompat.getColor(this, R.color.blue_sky)
    }

    private val sunsetSkyColor: Int by lazy{
        ContextCompat.getColor(this, R.color.sunset_sky)
    }

    private val nightSkyColor: Int by lazy {
        ContextCompat.getColor(this, R.color.night_sky)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.scene)
        sunView = findViewById(R.id.sun)
        skyView = findViewById(R.id.sky)

        sceneView.setOnClickListener {
            startAnimation()
        }
    }

    @SuppressLint("Recycle")
    private fun startAnimation() {
        val sunYStart = sunView.top.toFloat()
        // height 속성값은 bottom 속성값에서 top 속성값을 뺀 것과 같다.
        val sunYEnd = skyView.height.toFloat()


        if(flag) {
            val heightAnimator = ObjectAnimator
                .ofFloat(sunView, "y", sunYStart, sunYEnd)
                .setDuration(3000)

            heightAnimator.interpolator = AccelerateInterpolator()

            // 그냥 이렇게만 컬러 변경을 적용하면 색이 번쩍번쩍 바뀌게 된다.
            // 왜냐하면 컬러값이 단순한 숫자가 아나리 네 개의 부분으로 나뉘어져 있기 때문이다.
            // 따라서 ObjectAnimator 가 파란색과 주황색 사의 색상값을 올바르게 산출하게 해야 한다.
            val sunsetSkyAnimator = ObjectAnimator
                .ofInt(skyView, "backgroundColor", blueSkyColor, sunsetSkyColor)
                .setDuration(3000)
            sunsetSkyAnimator.setEvaluator(ArgbEvaluator()) // 이클래스를 사용하면 전체 값의 1/4 씩 산출해서 전달한다.

            val nightSkyAnimator = ObjectAnimator
                .ofInt(skyView, "backgroundColor", sunsetSkyColor, nightSkyColor)
                .setDuration(1500)
            nightSkyAnimator.setEvaluator(ArgbEvaluator())

            val animatorSet = AnimatorSet()
            animatorSet.play(heightAnimator)
                .with(sunsetSkyAnimator)
                .before(nightSkyAnimator)
            animatorSet.start()
            flag = false
        }else {
            val heightAnimator = ObjectAnimator
                .ofFloat(sunView, "y", sunYEnd, sunYStart)
                .setDuration(3000)
            heightAnimator.interpolator = AccelerateInterpolator()

            val raiseSunAnimator = ObjectAnimator
                .ofInt(skyView, "backgroundColor", nightSkyColor, sunsetSkyColor)
                .setDuration(3000)
            raiseSunAnimator.setEvaluator(ArgbEvaluator())

            val morningSkyAnimator = ObjectAnimator
                .ofInt(skyView, "backgroundColor", sunsetSkyColor, blueSkyColor)
                .setDuration(1500)
            morningSkyAnimator.setEvaluator(ArgbEvaluator())

            val animatorSet = AnimatorSet()
            animatorSet.play(raiseSunAnimator)
                .with(heightAnimator)
                .before(morningSkyAnimator)
            animatorSet.start()
            flag = true
        }
    }
}