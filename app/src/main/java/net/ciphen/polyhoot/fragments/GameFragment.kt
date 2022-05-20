package net.ciphen.polyhoot.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import kotlinx.serialization.json.*
import net.ciphen.polyhoot.R
import net.ciphen.polyhoot.databinding.FragmentGameBinding
import net.ciphen.polyhoot.game.WebSocketSession
import net.ciphen.polyhoot.game.event.GameEventType
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import kotlin.properties.Delegates

class GameFragment : Fragment(), Observer {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private var gameId by Delegates.notNull<Int>()
    private var name by Delegates.notNull<String>()
    private val webSocketSession = WebSocketSession.getInstance()
    private var answerBindings: MutableList<MaterialCardView> = mutableListOf()
    private var answered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webSocketSession.addObserver(this)
        gameId = requireArguments().getInt("GAME_UID")
        name = requireArguments().getString("NAME")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameText.text = name
        binding.gameIdText.text = getString(R.string.game_id, gameId)
        answerBindings.add(binding.answer1)
        answerBindings.add(binding.answer2)
        answerBindings.add(binding.answer3)
        answerBindings.add(binding.answer4)
    }

    override fun update(session: Observable, message: Any?) {
        Log.i("GameFragment", "Got update with message $message")
        val pair = message as Pair<*, *>
        val event = pair.first as GameEventType
        var args = ""
        if (pair.second != null) {
            args = pair.second as String
        }
        requireActivity().runOnUiThread {
            when (event) {
                GameEventType.START_GAME -> {
                    binding.gameStatusText.text = getString(R.string.starting_game)
                }
                GameEventType.QUESTION -> {
                    answered = false
                    val startTime = System.currentTimeMillis()
                    val duration = Json.parseToJsonElement(args).jsonObject["duration"]?.jsonPrimitive?.int ?: 10
                    binding.gameStatusText.visibility = View.GONE
                    binding.choicesLayout.visibility = View.VISIBLE
                    binding.waitingCircle.visibility = View.GONE
                    answerBindings.forEachIndexed { index, materialCardView ->
                        materialCardView.setOnClickListener {
                            val answerTime = System.currentTimeMillis()
                            val timeElapsed = answerTime - startTime
                            val score = 1000.0 - (timeElapsed / (duration * 1000.0)) * 1000.0
                            Log.i("GameFragment", "startTime: $startTime, answerTime: $answerTime, timeElapsed: $timeElapsed, score: $score")
                            webSocketSession.sendMessage(
                                JsonObject(
                                    mapOf(
                                        Pair("event", JsonPrimitive("answer")),
                                        Pair("answer", JsonPrimitive(index)),
                                        Pair("score", JsonPrimitive(score.toInt()))
                                    )
                                ).toString()
                            )
                            binding.gameStatusText.text = getString(R.string.answer_sent)
                            answered = true
                            binding.gameStatusText.visibility = View.VISIBLE
                            binding.choicesLayout.visibility = View.GONE
                            binding.waitingCircle.visibility = View.VISIBLE
                        }
                    }
                }
                GameEventType.TIME_UP -> {
                    answered = true
                    binding.gameStatusText.text = getString(R.string.time_up_text)
                    binding.choicesLayout.visibility = View.GONE
                    binding.gameStatusText.visibility = View.VISIBLE
                    binding.waitingCircle.visibility = View.VISIBLE
                }
                GameEventType.FORCE_STOP -> {
                    binding.choicesLayout.visibility = View.GONE
                    binding.waitingCircle.visibility = View.GONE
                    binding.gameStatusText.visibility = View.VISIBLE
                    binding.gameStatusText.text = getString(R.string.force_stop)
                }
                GameEventType.END -> {
                    binding.choicesLayout.visibility = View.GONE
                    binding.waitingCircle.visibility = View.GONE
                    binding.gameStatusText.visibility = View.VISIBLE
                    binding.gameStatusText.text = getString(R.string.game_ended)
                }
                GameEventType.GET_READY -> {
                    binding.choicesLayout.visibility = View.GONE
                    binding.waitingCircle.visibility = View.VISIBLE
                    binding.gameStatusText.visibility = View.VISIBLE
                    binding.gameStatusText.text = getString(R.string.get_ready)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        webSocketSession.removeObserver(this)
    }
}