package com.bignerdranch.criminalintent2

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt

fun getScaleBitmap(path:String, destWidth:Int, destHeight:Int): Bitmap { // 뷰의 크기가 들어온다.
    // 이미지 파일의 크기를 읽는다.
    var options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // 크기를 얼마나 줄일지 파악한다.
    // 예를 들어 inSampleSize가 1이면 원래 파일의 각 수평 화소당 하나의 최종 수평화소를 갖는다.
    // 그리고 2면 원래 파일의 두 개의 수평화소마다 하나의 수평화소를 갖는다. 따라서
    // inSampleSize 가 2일때 는 원래 이미지 화소의 1/4에 해당하는 화소 개수를 갖는 이미지가 된다.

    var inSampleSize = 1
    // 들어온 이미지가 Bitmap 에 고정된 width 보다 큰경우에만
    // scale 을 조정해준다. 작을 경우 그냥 sampleSize 를 1로 한다.
    if(srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        // width, height 중에 큰값을 기준으로 삼는다.
        val sampleScale = if(heightScale > widthScale) {
            heightScale
        }else {
            widthScale
        }
        // 반올림 해서 sampleSize 로 scale 한다.
        inSampleSize = sampleScale.roundToInt()
    }

    options = BitmapFactory.Options()
    // 옵션의 sampeSize 에 매핑한다.
    options.inSampleSize = inSampleSize

    // 최종 Bitmap을 생성한다.
    return BitmapFactory.decodeFile(path, options)
}

fun getScaledBitmap(path:String, activity: Activity):Bitmap {

    val displayMetrics = DisplayMetrics()
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.display?.getRealMetrics(displayMetrics)
        getScaleBitmap(path, displayMetrics.widthPixels, displayMetrics.heightPixels)
    }else {
        val display = activity.windowManager.defaultDisplay
        display.getMetrics(displayMetrics)
        getScaleBitmap(path, displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}