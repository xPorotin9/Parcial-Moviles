package com.example.parcial

data class AnswerResult(
    val isCorrect: Boolean,    // Indica si la respuesta es correcta
    val correctAnswer: String, // La respuesta correcta
    val explanation: String,   // Explicación de la respuesta
    val timeOut: Boolean = false // Indica si el tiempo se ha agotado
)