package com.example.parcial

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.parcial.databinding.FragmentQuestionBinding

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            provideErrorFeedback()
        }
    }

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
        binding.timeProgressBar.max = 30
        binding.timeProgressBar.progress = 30

        viewModel.initialize(requireContext())
        setupObservers()
        setupListeners()
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
            binding.answersRadioGroup.clearCheck()
        }

        viewModel.timeRemaining.observe(viewLifecycleOwner) { timeRemaining ->
            binding.timerTextView.text = getString(R.string.time_remaining, timeRemaining)
            binding.timeProgressBar.progress = timeRemaining
        }

        viewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            binding.progressTextView.text = getString(R.string.question_progress, index + 1, 6)
            playCurrentQuestionSound()
            binding.answersRadioGroup.clearCheck()
        }

        viewModel.answerResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                showAnswerResult(result)
            }
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
                checkVibrationPermissionAndProvideErrorFeedback()
            }
        }
    }

    private fun checkVibrationPermissionAndProvideErrorFeedback() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                provideErrorFeedback()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.VIBRATE) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.VIBRATE)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permiso necesario")
            .setMessage("Necesitamos el permiso de vibración para proporcionar feedback táctil")
            .setPositiveButton("Solicitar permiso") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.VIBRATE)
            }
            .setNegativeButton("No gracias") { dialog, _ ->
                dialog.dismiss()
                showErrorSnackbar()
            }
            .show()
    }

    private fun provideErrorFeedback() {
        showErrorSnackbar()

        try {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            // Si hay algún problema con la vibración, al menos el Snackbar se mostrará
        }
    }

    private fun showErrorSnackbar() {
        Snackbar.make(
            binding.root,
            "Debes seleccionar una respuesta antes de continuar",
            Snackbar.LENGTH_SHORT
        ).apply {
            setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error_color))
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            setActionTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }.show()
    }

    private fun moveToNextQuestion() {
        binding.answersRadioGroup.clearCheck()
        if (viewModel.currentQuestionIndex.value!! < 4) {
            viewModel.moveToNextQuestion()
        } else {
            findNavController().navigate(R.id.action_questionFragment_to_resultFragment)
        }
    }

    private fun showAnswerResult(result: AnswerResult) {
        val title = when {
            result.timeOut -> "Muy lento chamo"
            result.isCorrect -> "Tan cerebro!?"
            else -> "No le sabes"
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
                moveToNextQuestion()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        musicManager.stopMusic()
    }
}