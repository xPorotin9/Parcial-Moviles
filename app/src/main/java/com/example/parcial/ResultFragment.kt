package com.example.parcial

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parcial.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    // Variable de enlace con el layout (binding)
    private var _binding: FragmentResultBinding? = null
    // Acceso seguro a los elementos del layout a través de la propiedad `binding`
    private val binding get() = _binding!!
    // Instancia del ViewModel compartido para acceder al estado del quiz
    private val viewModel: QuizViewModel by activityViewModels()

    // Método para inflar la vista del fragmento
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout XML asociado a este fragmento usando ViewBinding
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Método para configurar la lógica después de que la vista ha sido creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observa los cambios en la puntuación del quiz y actualiza la UI
        viewModel.score.observe(viewLifecycleOwner) { score ->
            // Actualiza el texto de la puntuación en la interfaz
            binding.scoreTextView.text = getString(R.string.final_score, score, 5)

            // Configura el resultado basado en la puntuación obtenida
            val (imageRes, titleText, message) = when (score) {
                0 -> Triple(
                    R.drawable.yamcha,   // Imagen de resultado
                    "¡Eres igual de bajo que Yamcha!",  // Título del resultado
                    "Parece que necesitas estudiar bastante..."  // Mensaje del resultado
                )
                1 -> Triple(
                    R.drawable.monaka,
                    "¡Tienes el Monaka!",
                    "Sigue siendo un nivel bajo..."
                )
                2 -> Triple(
                    R.drawable.mrpopo,
                    "¡Nivel Mr Popo!",
                    "Ya vas decentemente"
                )
                3 -> Triple(
                    R.drawable.spopovich,
                    "¡Nivel pegalon!",
                    "¡Un poco más y la haces!"
                )
                4 -> Triple(
                    R.drawable.estrellas,
                    "¡Nivel bastante bien (como el personaje)!",
                    "Si le sabes, pero te falta una chiqui!"
                )
                else -> Triple(
                    R.drawable.gogeta,
                    "¡Nivel Gogeta!",
                    "Bachiller en lore de Dragon Ball"
                )
            }

            // Actualiza la UI con el resultado obtenido
            binding.resultImageView.setImageResource(imageRes)
            binding.resultTitleTextView.text = titleText
            binding.resultMessageTextView.text = message

            // Ejecuta una animación de aparición para los resultados
            animateResults()
        }

        // Configura el botón "Reiniciar" para reiniciar el juego
        binding.restartButton.setOnClickListener {
            viewModel.startNewGame()  // Reinicia el juego
            // Navega al fragmento de bienvenida
            findNavController().navigate(R.id.action_resultFragment_to_welcomeFragment)
        }

        // Configura el botón "Compartir" para compartir los resultados
        binding.shareButton.setOnClickListener {
            shareResults()  // Llama al método para compartir los resultados
        }
    }

    // Método para animar los resultados en pantalla
    private fun animateResults() {
        // Define una animación de desvanecimiento
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000  // Duración de la animación en milisegundos
            fillAfter = true  // Mantiene la animación al terminar
        }

        // Aplica la animación a los elementos de resultado
        binding.resultImageView.startAnimation(fadeIn)
        binding.resultTitleTextView.startAnimation(fadeIn)
        binding.resultMessageTextView.startAnimation(fadeIn)
    }

    // Método para compartir los resultados del quiz a través de otras aplicaciones
    private fun shareResults() {
        // Obtiene la puntuación actual
        val score = viewModel.score.value ?: 0
        // Construye el texto a compartir
        val shareText = """
            ¡He completado el Quiz de Dragon Ball!
            Mi puntuación: $score/5
            ${binding.resultTitleTextView.text}
            ${binding.resultMessageTextView.text}
        """.trimIndent()

        // Crea un intent para compartir el resultado
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"  // Especifica el tipo de contenido a compartir
            putExtra(Intent.EXTRA_TEXT, shareText)  // Añade el texto a compartir
        }
        // Inicia la actividad para compartir el contenido
        startActivity(Intent.createChooser(shareIntent, "Compartir resultado"))
    }

    // Limpia la referencia del binding cuando se destruye la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Evita fugas de memoria eliminando el binding
    }
}
