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
    // Binding para acceder a los elementos del layout
    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    // ViewModel para gestionar el estado del quiz
    private val viewModel: QuizViewModel by activityViewModels()

    // Manager de música de fondo
    private lateinit var musicManager: BackgroundMusicManager

    // Lista de sonidos para cada pregunta
    private val questionSounds = arrayOf(
        R.raw.merevelo,
        R.raw.ando,
        R.raw.bellaco,
        R.raw.once,
        R.raw.esencia
    )

    // Solicitador de permisos de vibración
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Si el permiso es concedido, se proporciona feedback por vibración
        if (isGranted) {
            provideErrorFeedback()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa el manager de música
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
        // Configura la barra de progreso del temporizador
        binding.timeProgressBar.max = 30
        binding.timeProgressBar.progress = 30

        // Inicializa el ViewModel y configura los observadores
        viewModel.initialize(requireContext())
        setupObservers()
        setupListeners()
        playCurrentQuestionSound() // Reproduce el sonido de la pregunta actual
    }

    // Configura los observadores para los LiveData en el ViewModel
    private fun setupObservers() {
        // Observa la pregunta actual y actualiza la UI
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            binding.questionTextView.text = question.questionText
            binding.questionImageView.setImageResource(question.imageResId)

            // Limpia y actualiza las opciones de respuesta en los radio buttons
            binding.answersRadioGroup.removeAllViews()
            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(context).apply {
                    id = index
                    text = option
                    textSize = 16f
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 16 }
                }
                binding.answersRadioGroup.addView(radioButton)
            }
            binding.answersRadioGroup.clearCheck()
        }

        // Observa el tiempo restante y actualiza el temporizador en pantalla
        viewModel.timeRemaining.observe(viewLifecycleOwner) { timeRemaining ->
            binding.timerTextView.text = getString(R.string.time_remaining, timeRemaining)
            binding.timeProgressBar.progress = timeRemaining
        }

        // Observa el índice de la pregunta actual para cambiar de sonido y actualizar la UI
        viewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            binding.progressTextView.text = getString(R.string.question_progress, index + 1, 6)
            playCurrentQuestionSound() // Cambia el sonido de la pregunta
            binding.answersRadioGroup.clearCheck() // Limpia la selección
        }

        // Observa el resultado de la respuesta
        viewModel.answerResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                showAnswerResult(result) // Muestra el resultado de la respuesta
            }
        }
    }

    // Reproduce el sonido de la pregunta actual
    private fun playCurrentQuestionSound() {
        viewModel.currentQuestionIndex.value?.let { index ->
            if (index < questionSounds.size) {
                musicManager.playOneShot(questionSounds[index])
            }
        }
    }

    // Configura los listeners de los botones
    private fun setupListeners() {
        binding.nextButton.setOnClickListener {
            val selectedAnswerId = binding.answersRadioGroup.checkedRadioButtonId
            if (selectedAnswerId != -1) {
                viewModel.submitAnswer(selectedAnswerId) // Envia la respuesta seleccionada
            } else {
                checkVibrationPermissionAndProvideErrorFeedback() // Verifica permisos para vibrar si no hay selección
            }
        }
    }

    // Verifica si el permiso de vibración está concedido y lo solicita si es necesario
    private fun checkVibrationPermissionAndProvideErrorFeedback() {
        when {
            // Si ya tiene el permiso, proporciona feedback
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                provideErrorFeedback()
            }
            // Si debe mostrar una explicación del permiso
            shouldShowRequestPermissionRationale(Manifest.permission.VIBRATE) -> {
                showPermissionRationaleDialog()
            }
            // Si no, solicita el permiso
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.VIBRATE)
            }
        }
    }

    // Muestra un diálogo explicando por qué se necesita el permiso de vibración
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

    // Proporciona feedback táctil al usuario en caso de error
    private fun provideErrorFeedback() {
        showErrorSnackbar() // Muestra un mensaje de error

        try {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {
            // Si hay un problema con la vibración, al menos se muestra el Snackbar
        }
    }

    // Muestra un Snackbar cuando el usuario no selecciona una respuesta
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

    // Avanza a la siguiente pregunta o al fragmento de resultados
    private fun moveToNextQuestion() {
        binding.answersRadioGroup.clearCheck()
        if (viewModel.currentQuestionIndex.value!! < 4) {
            viewModel.moveToNextQuestion()
        } else {
            findNavController().navigate(R.id.action_questionFragment_to_resultFragment)
        }
    }

    // Muestra el resultado de la respuesta en un diálogo
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
                moveToNextQuestion() // Pasa a la siguiente pregunta
            }
            .setCancelable(false)
            .show()
    }

    // Pausa la música cuando el fragmento está en pausa
    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    // Reanuda la música cuando el fragmento está en primer plano
    override fun onResume() {
        super.onResume()
    }

    // Libera los recursos y detiene la música cuando el fragmento se destruye
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        musicManager.stopMusic()
    }
}
