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

    private val _currentQuestionIndex = MutableLiveData(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _answerResult = MutableLiveData<AnswerResult>()
    val answerResult: LiveData<AnswerResult> = _answerResult

    private var timerJob: Job? = null
    private var questions: List<Question> = listOf()

    // Listade imágenes para cada pregunta
    private val questionImages = listOf(
        R.drawable.revelo,
        R.drawable.sadala,
        R.drawable.patrulla,
        R.drawable.mono,
        R.drawable.jiren,
        R.drawable.esencia
    )

    fun initialize(context: Context) {
        if (questions.isEmpty()) {
            loadQuestions(context)
        }
        startNewGame()
    }

    private fun loadQuestions(context: Context) {
        val questionTexts = context.resources.getStringArray(R.array.questions)
        val answersList = context.resources.getStringArray(R.array.answers)
        val correctAnswers = context.resources.getStringArray(R.array.correct_answers)
        val explanations = context.resources.getStringArray(R.array.explanations)

        questions = questionTexts.mapIndexed { index, questionText ->
            Question(
                questionText = questionText,
                options = answersList[index].split("|"),
                correctAnswerIndex = correctAnswers[index].toInt(),
                explanation = explanations[index],
                imageResId = questionImages[index]
            )
        }
    }

    fun startNewGame() {
        _score.value = 0
        _currentQuestionIndex.value = 0
        loadCurrentQuestion()
    }

    private fun loadCurrentQuestion() {
        _currentQuestionIndex.value?.let { index ->
            if (index < questions.size) {
                _currentQuestion.value = questions[index]
                startTimer()
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
            handleTimeOut()
        }
    }

    private fun handleTimeOut() {
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