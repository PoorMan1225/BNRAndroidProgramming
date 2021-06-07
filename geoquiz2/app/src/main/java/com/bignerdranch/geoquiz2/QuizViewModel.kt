package com.bignerdranch.geoquiz2

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel() {

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true)
    )

    var currentIndex = 0
        set(value) {
            if(currentIndex != value) {
                field = value
            }
        }

    var cheatCount = 3

    var questionIsCheat
        get() = questionBank[currentIndex].isCheat
        set(value) {
            questionBank[currentIndex].isCheat = value
        }

    var total = 0f

    val allResolve
        get() = questionBank.filter { it.resolve }.size == questionBank.size

    val questionBankSize
        get() = questionBank.size

    var currentQuestionResolve:Boolean
        get() = questionBank[currentIndex].resolve
        set(value) {
            questionBank[currentIndex].resolve = value
        }

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText:Int
        get() = questionBank[currentIndex].textResId

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    fun moveBack() {
        currentIndex = if(currentIndex.dec() < 0) questionBank.size.dec() else currentIndex.dec()
    }
}