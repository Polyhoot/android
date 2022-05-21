/*
 * Copyright 2022 Arseniy Graur
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ciphen.polyhoot.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import kotlinx.serialization.json.int
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonPrimitive
import net.ciphen.polyhoot.R
import net.ciphen.polyhoot.databinding.FragmentGameBinding
import net.ciphen.polyhoot.game.WebSocketSession
import net.ciphen.polyhoot.game.event.GameEventType
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import kotlin.properties.Delegates

private const val DEFAULT_DURATION = 10
private const val MAX_SCORE = 1000

class GameFragment : Fragment(), Observer {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private var gameId by Delegates.notNull<Int>()
    private var name by Delegates.notNull<String>()
    private val webSocketSession = WebSocketSession.getInstance()
    private var answerBindings: MutableList<MaterialCardView> = mutableListOf()
    private var answered: Boolean = false
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webSocketSession.addObserver(this)
        gameId = requireArguments().getInt("GAME_UID")
        name = requireArguments().getString("NAME")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        keepScreenOn(true)
        binding.nameText.text = name
        binding.gameIdText.text = getString(R.string.game_id, gameId)
        updateScore()
        answerBindings.add(binding.answer1)
        answerBindings.add(binding.answer2)
        answerBindings.add(binding.answer3)
        answerBindings.add(binding.answer4)
    }

    private fun sendAnswer(answer: Int, score: Int) {
        webSocketSession.sendMessage(
            JsonObject(
                mapOf(
                    Pair("event", JsonPrimitive("answer")),
                    Pair("answer", JsonPrimitive(answer)),
                    Pair("score", JsonPrimitive(score.toInt()))
                )
            ).toString()
        )
        binding.gameStatusText.text = getString(R.string.answer_sent)
        answered = true
        choicesUi(false)
        progressCircle(true)
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
                    val duration = Json
                        .parseToJsonElement(args)
                        .jsonObject["duration"]
                        ?.jsonPrimitive
                        ?.int ?: DEFAULT_DURATION
                    val text = Json.parseToJsonElement(args)
                        .jsonObject["text"]
                        ?.jsonPrimitive
                        ?.content ?: ""
                    if (text != "") {
                        binding.questionText.text = text
                        binding.questionText.visibility = View.VISIBLE
                    } else {
                        binding.questionText.visibility = View.GONE
                    }
                    choicesUi(true)
                    progressCircle(false)
                    answerBindings.forEachIndexed { index, materialCardView ->
                        materialCardView.setOnClickListener {
                            val answerTime = System.currentTimeMillis()
                            val timeElapsed = answerTime - startTime
                            val score = MAX_SCORE - (timeElapsed / duration)
                            sendAnswer(index, score.toInt())
                        }
                    }
                }
                GameEventType.TIME_UP -> {
                    answered = true
                    val newScore = Json.parseToJsonElement(args)
                        .jsonObject["score"]
                        ?.jsonPrimitive
                        ?.int
                        ?: 0
                    if (score != newScore) {
                        score = newScore
                        updateScore()
                        binding.gameStatusText.text = getString(R.string.answer_right)
                    } else {
                        binding.gameStatusText.text = getString(R.string.answer_wrong)
                    }
                    choicesUi(false)
                    progressCircle(true)
                }
                GameEventType.FORCE_STOP -> {
                    keepScreenOn(false)
                    choicesUi(false)
                    progressCircle(false)
                    binding.gameStatusText.text = getString(R.string.force_stop)
                }
                GameEventType.END -> {
                    keepScreenOn(false)
                    choicesUi(false)
                    progressCircle(false)
                    binding.gameStatusText.text = getString(R.string.game_ended)
                }
                GameEventType.GET_READY -> {
                    choicesUi(false)
                    progressCircle(true)
                    binding.gameStatusText.text = getString(R.string.get_ready)
                }
            }
        }
    }

    private fun updateScore() {
        binding.scoreText.text = getString(R.string.score_text, score)
    }

    private fun choicesUi(show: Boolean) {
        if (show) {
            binding.choicesLayout.visibility = View.VISIBLE
            binding.gameStatusText.visibility = View.GONE
        } else {
            binding.choicesLayout.visibility = View.GONE
            binding.gameStatusText.visibility = View.VISIBLE
        }
    }

    private fun progressCircle(show: Boolean) {
        if (show) {
            binding.waitingCircle.visibility = View.VISIBLE
        } else {
            binding.waitingCircle.visibility = View.GONE
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
        keepScreenOn(false)
    }

    private fun keepScreenOn(keep: Boolean) {
        if (keep) {
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
