package com.example.composition.presentation

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.example.composition.R
import com.example.composition.databinding.GameFragmentBinding
import com.example.composition.domain.entites.GameResult
import com.example.composition.domain.entites.Level

class GameFragment : Fragment() {

    private lateinit var level: Level

    private val viewModel by lazy { //при первом обращении к viewModel будет проициализирован этим значением
        ViewModelProvider(
            this,
            AndroidViewModelFactory.getInstance(requireActivity().application)
        )[GameViewModel::class.java]
    }

    private val tvOption by lazy {
        mutableListOf<TextView>().apply {
            add(binding.tvOption1)
            add(binding.tvOption2)
            add(binding.tvOption3)
            add(binding.tvOption4)
            add(binding.tvOption5)
            add(binding.tvOption6)
        }
    }

    private var _binding: GameFragmentBinding? = null
    private val binding: GameFragmentBinding
        get() = _binding ?: throw RuntimeException("GameFragmentBinding == null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setClickListenersToOption()
        viewModel.startGame(level)
    }

    private fun setClickListenersToOption() { //установка слушателей на выбор ответа
        for (tvOption in tvOption) {
            tvOption.setOnClickListener {
                viewModel.chooseAnswer(tvOption.text.toString().toInt())
            }
        }
    }

    private fun observeViewModel() {

        viewModel.question.observe(viewLifecycleOwner) {
            binding.tvSum.text = it.sum.toString() // устанавливаем значение суммы
            binding.tvLeftNumber.text =
                it.visibleNumber.toString() // устанавливаем значение видимого числа
            for (i in 0 until tvOption.size) { // устанавливаем значение вариантов ответов
                tvOption[i].text = it.option[i].toString()
            }
        }

        viewModel.percentOfRightAnswers.observe(viewLifecycleOwner) { // процент правильных ответов в прогресс баре
            binding.progressBar.setProgress(it, true)
        }

        viewModel.progressAnswers.observe(viewLifecycleOwner) {
            binding.tvProgressAnswer.text = it
        }

        viewModel.enoughAnswers.observe(viewLifecycleOwner) {
            binding.tvProgressAnswer.setTextColor(getColorByState(it))
        }

        viewModel.enoughPercent.observe(viewLifecycleOwner) {
            val color = getColorByState(it)
            binding.progressBar.progressTintList = ColorStateList.valueOf(color) //установка цвета
            //прогресс бара
        }

        viewModel.formattedTime.observe(viewLifecycleOwner) {
            binding.tvTimer.text = it
        }

        viewModel.minPercent.observe(viewLifecycleOwner) {
            binding.progressBar.secondaryProgress = it
        }

        viewModel.gameResult.observe(viewLifecycleOwner) {
            launchGameFinishedFragment(it)
        }
    }

    private fun getColorByState(goodState: Boolean): Int {
        val colorResId = if (goodState) {
            android.R.color.holo_green_light
        } else {
            android.R.color.holo_red_light
        }
        return ContextCompat.getColor(requireContext(), colorResId) // установка цвета текста в
        // зависимости от достаточности количества ответов и процента правильных ответов
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseArgs() {
        requireArguments().getParcelable<Level>(KEY_LEVEL)?.let {
            level = it
        }
    }

    private fun launchGameFinishedFragment(gameResult: GameResult) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, GameFinishedFragment.newInstance(gameResult))
            .addToBackStack(null)
            .commit()
    }

    companion object {

        const val NAME = "GameFragment"
        private const val KEY_LEVEL = "level"

        fun newInstance(level: Level): GameFragment {
            return GameFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_LEVEL, level)
                }
            }
        }
    }
}