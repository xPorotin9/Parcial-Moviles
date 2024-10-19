package com.example.parcial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.parcial.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
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
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Iniciar la música del menú principal
        musicManager.startMusic(R.raw.quiz_music)

        binding.startButton.setOnClickListener {
            musicManager.stopMusic() // Detener la música antes de navegar
            findNavController().navigate(R.id.action_welcomeFragment_to_questionFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        musicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        musicManager.resumeMusic()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        musicManager.stopMusic()
    }
}
