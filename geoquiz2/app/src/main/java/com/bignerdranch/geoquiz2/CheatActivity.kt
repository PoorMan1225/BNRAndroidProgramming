package com.bignerdranch.geoquiz2

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

private const val EXTRA_ANSWER_IS_TRUE = "com.bignerdranch.android.geoquiz.answer_is_true"
private const val EXTRA_CHEAT_SHOW = "com.bignerdranch.android.geoquiz.cheat_result"
const val EXTRA_ANSWER_SHOWN = "com.bignerdranch.android.geoquiz.answer_shown"

class CheatActivity : AppCompatActivity() {

    private var answerIsTrue = false
    private var resultCheat:Int = 0
    private lateinit var answerTextView:TextView
    private lateinit var showAnswerButton: Button
    private lateinit var cheatCountTextView:TextView

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)

        initViews()
        saveData(savedInstanceState)
        initialize()

        answerIsTrue = intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false)
        showAnswerButton.setOnClickListener {
            resultCheat = when(answerIsTrue) {
                true -> R.string.true_button
                false -> R.string.false_button
            }
            answerTextView.setText(resultCheat)
            updateCheatCount()
            cheatButtonNotEnable()
            setAnswerShownResult()
        }
    }

    private fun updateCheatCount() {
        quizViewModel.cheatCount -= 1
        cheatCountTextView.text = quizViewModel.cheatCount.toString()
    }

    private fun initialize() {
        cheatCountTextView.text = quizViewModel.cheatCount.toString()
        cheatButtonNotEnable()
    }

    private fun cheatButtonNotEnable() {
        if (quizViewModel.cheatCount == 0) {
            showAnswerButton.isEnabled = false
        }
    }

    private fun saveData(savedInstanceState: Bundle?) {
        val resultCheatData = savedInstanceState?.getInt(EXTRA_CHEAT_SHOW) ?: 0
        resultCheat = resultCheatData
        answerTextView.text = if(resultCheatData != 0) getString(resultCheatData) else ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(EXTRA_CHEAT_SHOW, resultCheat)
    }

    private fun setAnswerShownResult() {
        // 결과를 돌려주기 위해 새로운 인텐트 생성.
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, true)
        }
        setResult(Activity.RESULT_OK, data)
    }

    private fun initViews() {
        answerTextView = findViewById(R.id.answer_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)
        cheatCountTextView = findViewById(R.id.cheatCountTextView)    }

    companion object {
        fun newIntent(packageContext:Context, answerIsTrue:Boolean): Intent {
            return Intent(packageContext, CheatActivity::class.java)
                .putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue)

        }
    }
}
