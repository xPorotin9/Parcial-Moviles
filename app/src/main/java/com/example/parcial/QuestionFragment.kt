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

    private val questionSounds = arrayOf(
        R.raw.merevelo,
        R.raw.ando,
        R.raw.bellaco,
        R.raw.once,
        R.raw.esencia
    )

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

        // Configurar el ProgressBar
        binding.timeProgressBar.max = 30
        binding.timeProgressBar.progress = 30

        viewModel.initialize(requireContext())
        setupObservers()
        setupListeners()

        // Reproducir el audio de la primera pregunta inmediatamente
        playCurrentQuestionSound()
    }

    private fun setupObservers() {
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            binding.questionTextView.text = question.questionText
            binding.questionImageView.setImageResource(question.imageResId)

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
                        topMargin = 16
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
            binding.progressTextView.text = getString(R.string.question_progress, index + 1, 5)
            playCurrentQuestionSound()
        }

        viewModel.answerResult.observe(viewLifecycleOwner) { result ->
            showAnswerResult(result)
        }
    }

    private fun playCurrentQuestionSound() {
        viewModel.currentQuestionIndex.value?.let { index ->
            if (index < questionSounds.size) {
                musicManager.playOneShot(questionSounds[index])
            }
        }
    }

    private fun setupListeners() {
        binding.nextButton.setOnClickListener {
            val selectedAnswerId = binding.answersRadioGroup.checkedRadioButtonId
            if (selectedAnswerId != -1) {
                viewModel.submitAnswer(selectedAnswerId)
            } else {
                Toast.makeText(context, "Escoge una respuesta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAnswerResult(result: AnswerResult) {
        val title = when {
            result.timeOut -> "Muy lento chamo"
            result.isCorrect -> "Tan cerebro!?"
            else -> "No se sabes!"
        }

        val message = buildString {
            if (!result.isCorrect) {
                appendLine("La respuesta correcta era: ${result.correctAnswer}")
                appendLine()
            }
            append("De hecho: ${result.explanation}")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()
                musicManager.stopMusic()
                if (viewModel.currentQuestionIndex.value!! < 4) {
                    viewModel.moveToNextQuestion()
                } else {
                    findNavController().navigate(R.id.action_questionFragment_to_resultFragment)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        // No llamamos a resumeMusic() aquí porque estamos usando playOneShot
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        musicManager.stopMusic()
    }
}