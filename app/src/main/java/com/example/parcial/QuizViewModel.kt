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
    // Tiempo máximo para responder cada pregunta
    private val TIMER_MAX_SECONDS = 30

    // LiveData para la pregunta actual
    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question> = _currentQuestion

    // LiveData para la puntuación del jugador
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    // LiveData para el tiempo restante
    private val _timeRemaining = MutableLiveData(TIMER_MAX_SECONDS)
    val timeRemaining: LiveData<Int> = _timeRemaining

    // LiveData para el índice de la pregunta actual
    private val _currentQuestionIndex = MutableLiveData(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    // LiveData para el resultado de la respuesta
    private val _answerResult = MutableLiveData<AnswerResult?>()
    val answerResult: LiveData<AnswerResult?> = _answerResult

    // Variable para gestionar el trabajo del temporizador
    private var timerJob: Job? = null

    // Lista de preguntas del quiz
    private var questions: List<Question> = listOf()

    // Imágenes asociadas a las preguntas
    private val questionImages = listOf(
        R.drawable.revelo,
        R.drawable.cell,
        R.drawable.vegeta,
        R.drawable.jiren,
        R.drawable.esencia
    )

    // Inicializa el ViewModel cargando las preguntas y comenzando un nuevo juego
    fun initialize(context: Context) {
        if (questions.isEmpty()) {
            loadQuestions(context)  // Cargar preguntas desde los recursos
        }
        startNewGame()  // Iniciar un nuevo juego
    }

    // Cargar las preguntas desde los recursos
    private fun loadQuestions(context: Context) {
        val questionTexts = context.resources.getStringArray(R.array.questions)
        val answersList = context.resources.getStringArray(R.array.answers)
        val correctAnswers = context.resources.getStringArray(R.array.correct_answers)
        val explanations = context.resources.getStringArray(R.array.explanations)

        // Mapear los textos de las preguntas a objetos Question
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

    // Iniciar un nuevo juego, reiniciando el estado
    fun startNewGame() {
        _score.value = 0
        _currentQuestionIndex.value = 0
        _answerResult.value = null
        loadCurrentQuestion()  // Cargar la primera pregunta
    }

    // Cargar la pregunta actual basada en el índice
    private fun loadCurrentQuestion() {
        _currentQuestionIndex.value?.let { index ->
            if (index < questions.size) {
                _currentQuestion.value = questions[index]  // Establecer la pregunta actual
                resetAndStartTimer()  // Reiniciar y comenzar el temporizador
            }
        }
    }

    // Reinicia el temporizador y comienza de nuevo
    private fun resetAndStartTimer() {
        timerJob?.cancel()  // Cancelar el temporizador anterior si existe
        _timeRemaining.value = TIMER_MAX_SECONDS  // Reiniciar el tiempo
        startTimer()  // Comenzar el nuevo temporizador
    }

    // Inicia un temporizador para contar el tiempo restante
    private fun startTimer() {
        timerJob = viewModelScope.launch {
            try {
                while (_timeRemaining.value!! > 0) {
                    delay(1000)  // Espera de 1 segundo
                    _timeRemaining.postValue(_timeRemaining.value!! - 1)  // Decrementa el tiempo
                }
                handleTimeOut()  // Manejar la situación si se acaba el tiempo
            } catch (e: Exception) {
                // Manejar excepciones del temporizador
            }
        }
    }

    // Manejar el caso cuando se acaba el tiempo
    private fun handleTimeOut() {
        _currentQuestion.value?.let { question ->
            _answerResult.postValue(AnswerResult(
                isCorrect = false,
                correctAnswer = question.options[question.correctAnswerIndex],  // Respuesta correcta
                explanation = question.explanation,  // Explicación de la respuesta
                timeOut = true  // Indica que se acabó el tiempo
            ))
        }
    }

    // Método para enviar la respuesta seleccionada por el usuario
    fun submitAnswer(selectedAnswerIndex: Int) {
        timerJob?.cancel()  // Detener el temporizador al enviar la respuesta

        _currentQuestion.value?.let { question ->
            // Verifica si la respuesta seleccionada es correcta
            val isCorrect = selectedAnswerIndex == question.correctAnswerIndex
            if (isCorrect) {
                _score.value = _score.value!! + 1  // Incrementar la puntuación si es correcta
            }

            // Publica el resultado de la respuesta
            _answerResult.value = AnswerResult(
                isCorrect = isCorrect,
                correctAnswer = question.options[question.correctAnswerIndex],
                explanation = question.explanation,
                timeOut = false
            )
        }
    }

    // Método para pasar a la siguiente pregunta
    fun moveToNextQuestion() {
        _currentQuestionIndex.value = _currentQuestionIndex.value!! + 1  // Incrementar el índice
        _answerResult.value = null  // Reiniciar el resultado de la respuesta
        loadCurrentQuestion()  // Cargar la siguiente pregunta
    }

    // Limpiar y cancelar el temporizador cuando se destruye el ViewModel
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()  // Cancelar cualquier temporizador activo
    }
}
