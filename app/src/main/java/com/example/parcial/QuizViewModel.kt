package com.example.parcial

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {
    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _timeRemaining = MutableLiveData(30)
    val timeRemaining: LiveData<Int> = _timeRemaining

    private val _gameFinished = MutableLiveData(false)
    val gameFinished: LiveData<Boolean> = _gameFinished

    private val _currentQuestionIndex = MutableLiveData(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _answerResult = MutableLiveData<AnswerResult>()
    val answerResult: LiveData<AnswerResult> = _answerResult

    private var timerJob: Job? = null
    private var questions: List<Question> = listOf()
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
        loadQuestions()
        startNewGame()
    }

    private fun loadQuestions() {
        val questionTexts = context.resources.getStringArray(R.array.questions)
        val answersList = context.resources.getStringArray(R.array.answers)
        val correctAnswers = context.resources.getStringArray(R.array.correct_answers)
        val explanations = context.resources.getStringArray(R.array.explanations)

        questions = questionTexts.mapIndexed { index, questionText ->
            Question(
                questionText = questionText,
                options = answersList[index].split("|"),
                correctAnswerIndex = correctAnswers[index].toInt(),
                explanation = explanations[index]
            )
        }
    }

    fun startNewGame() {
        _score.value = 0
        _currentQuestionIndex.value = 0
        _gameFinished.value = false
        loadCurrentQuestion()
    }

    private fun loadCurrentQuestion() {
        _currentQuestionIndex.value?.let { index ->
            if (index < questions.size) {
                _currentQuestion.value = questions[index]
                startTimer()
            } else {
                _gameFinished.value = true
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timeRemaining.value = 30
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value!! > 0) {
                delay(1000)
                _timeRemaining.value = _timeRemaining.value!! - 1
            }
            timeOut()
        }
    }

    private fun timeOut() {
        _currentQuestion.value?.let { question ->
            _answerResult.value = AnswerResult(
                isCorrect = false,
                correctAnswer = question.options[question.correctAnswerIndex],
                explanation = question.explanation,
                timeOut = true
            )
        }
    }

    fun submitAnswer(selectedAnswerIndex: Int) {
        timerJob?.cancel()
        _currentQuestion.value?.let { question ->
            val isCorrect = selectedAnswerIndex == question.correctAnswerIndex
            if (isCorrect) {
                _score.value = _score.value!! + 1
            }

            _answerResult.value = AnswerResult(
                isCorrect = isCorrect,
                correctAnswer = question.options[question.correctAnswerIndex],
                explanation = question.explanation,
                timeOut = false
            )
        }
    }

    fun moveToNextQuestion() {
        _currentQuestionIndex.value = _currentQuestionIndex.value!! + 1
        loadCurrentQuestion()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}