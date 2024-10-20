package com.example.parcial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.parcial.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    // Variable para el enlace con el layout (ViewBinding)
    private var _binding: FragmentWelcomeBinding? = null
    // Propiedad de acceso seguro para los elementos del layout
    private val binding get() = _binding!!

    // Instancia del administrador de música de fondo
    private lateinit var musicManager: BackgroundMusicManager

    // Método que se llama al crear el fragmento, inicializando el administrador de música
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtiene la instancia del administrador de música
        musicManager = BackgroundMusicManager.getInstance(requireContext())
    }

    // Infla el layout del fragmento utilizando ViewBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout XML asociado con el fragmento
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Configura el comportamiento de la UI cuando la vista ya ha sido creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicia la música de fondo (en bucle) cuando se muestra el fragmento de bienvenida
        musicManager.startMusic(R.raw.quiz_music)

        // Configura el botón de inicio para detener la música y navegar al siguiente fragmento
        binding.startButton.setOnClickListener {
            musicManager.stopMusic()  // Detiene la música antes de cambiar de pantalla
            // Navega al fragmento de preguntas (QuestionFragment)
            findNavController().navigate(R.id.action_welcomeFragment_to_questionFragment)
        }
    }

    // Pausa la música cuando el fragmento pasa al fondo (por ejemplo, al cambiar de app)
    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    // Reanuda la música cuando el fragmento vuelve a estar en primer plano
    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    // Se asegura de liberar el binding y detener la música cuando la vista se destruye
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Limpia el binding para evitar fugas de memoria
        musicManager.stopMusic()  // Detiene la música si la vista es destruida
    }
}
