package com.bignerdranch.geoquiz2

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var cheatButton:Button
    private lateinit var trueButton:Button
    private lateinit var falseButton:Button
    private lateinit var nextButton:Button
    private lateinit var previousButton:Button
    private lateinit var questionTextView:TextView

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        initViews()
        updateQuestion()
        setOnListener()
        changeButtonState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK) {
            return
        }

        if(requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.questionIsCheat =
                data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    private fun initViews() {
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)
    }

    private fun setOnListener() {
        trueButton.setOnClickListener {
            checkAnswer(true)
            allResolve()
        }
        falseButton.setOnClickListener {
            checkAnswer(false)
            allResolve()
        }
        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            changeButtonState()
            updateQuestion()
        }
        previousButton.setOnClickListener {
            quizViewModel.moveBack()
            changeButtonState()
            updateQuestion()
        }
        cheatButton.setOnClickListener {
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }
    }

    private fun allResolve() {
        Log.d(TAG, "${quizViewModel.allResolve}")
        if (quizViewModel.allResolve) {
            Toast.makeText(this, "${"%.1f".format(quizViewModel.total)} 점", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeButtonState() {
        trueButton.isEnabled = quizViewModel.currentQuestionResolve.not()
        falseButton.isEnabled = quizViewModel.currentQuestionResolve.not()
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

        val messageId = when {
            quizViewModel.questionIsCheat -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        sumScore(messageId)
        quizViewModel.currentQuestionResolve = true
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show()
    }

    private fun sumScore(messageId: Int) {
        if(messageId == R.string.correct_toast && quizViewModel.currentQuestionResolve.not())
            quizViewModel.total += (100.0 / quizViewModel.questionBankSize).toFloat()
    }

    private fun updateQuestion() {
        questionTextView.setText(quizViewModel.currentQuestionText)
    }
}