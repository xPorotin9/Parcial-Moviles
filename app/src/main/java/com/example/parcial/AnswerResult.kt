package com.example.parcial

data class AnswerResult(
    val isCorrect: Boolean,
    val correctAnswer: String,
    val explanation: String,
    val timeOut: Boolean
)
