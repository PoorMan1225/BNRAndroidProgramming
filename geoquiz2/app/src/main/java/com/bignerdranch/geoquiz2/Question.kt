package com.bignerdranch.geoquiz2

import androidx.annotation.StringRes

data class Question(@StringRes val textResId: Int, val answer:Boolean) {
    var resolve: Boolean = false
    var isCheat:Boolean = false
}