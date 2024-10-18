package com.example.parcial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parcial.databinding.FragmentQuestionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QuestionFragment : Fragment() {
    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by activityViewModels()
    private lateinit var musicManager: BackgroundMusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicManager = BackgroundMusicManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(requireContext())
        setupObservers()
        setupListeners()
        musicManager.startMusic()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    private fun setupObservers() {
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            binding.questionTextView.text = question.questionText
            binding.answersRadioGroup.removeAllViews()

            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(context).apply {
                    id = index
                    text = option
                    textSize = 16f
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8
                    }
                }
                binding.answersRadioGroup.addView(radioButton)
            }
        }

        viewModel.timeRemaining.observe(viewLifecycleOwner) { timeRemaining ->
            binding.timerTextView.text = getString(R.string.time_remaining, timeRemaining)
            binding.timeProgressBar.progress = timeRemaining
        }

        viewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            val totalQuestions = 6 // O el número total de preguntas que tengas
            binding.progressTextView.text = getString(R.string.question_progress, index + 1, totalQuestions)
            binding.questionProgressBar.progress = ((index + 1) * 100) / totalQuestions
        }

        viewModel.answerResult.observe(viewLifecycleOwner) { result ->
            showAnswerResult(result)
        }
    }

    private fun setupListeners() {
        binding.nextButton.setOnClickListener {
            val selectedAnswerId = binding.answersRadioGroup.checkedRadioButtonId
            if (selectedAnswerId != -1) {
                viewModel.submitAnswer(selectedAnswerId)
            } else {
                Toast.makeText(context, "Por favor selecciona una respuesta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAnswerResult(result: AnswerResult) {
        val title = when {
            result.timeOut -> "¡Tiempo agotado!"
            result.isCorrect -> "¡Respuesta Correcta!"
            else -> "¡Respuesta Incorrecta!"
        }

        val message = buildString {
            if (!result.isCorrect) {
                appendLine("La respuesta correcta era: ${result.correctAnswer}")
                appendLine()
            }
            append("Explicación: ${result.explanation}")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()
                if (viewModel.currentQuestionIndex.value!! < 4) { // 4 es el índice de la última pregunta (total 5)
                    viewModel.moveToNextQuestion()
                } else {
                    findNavController().navigate(R.id.action_questionFragment_to_resultFragment)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}