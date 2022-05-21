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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.snackbar.Snackbar
import net.ciphen.polyhoot.R
import net.ciphen.polyhoot.databinding.FragmentJoinGameBinding
import net.ciphen.polyhoot.game.WebSocketSession
import net.ciphen.polyhoot.game.event.GameEventType
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import kotlin.properties.Delegates

private const val NO_SUCH_GAME_SNACKBAR_DURATION = 5000

class JoinGameFragment : Fragment(), Observer {
    private var _binding: FragmentJoinGameBinding? = null
    private val binding get() = _binding!!
    private var webSocketSession = WebSocketSession.getInstance()
    private var gameId by Delegates.notNull<Int>()
    private var name by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameId = requireArguments().getInt(
            "GAME_UID"
        )
        webSocketSession.addObserver(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.statusText.text = getString(R.string.status, "NOT CONNECTED")
        binding.gameJoin.setOnClickListener {
            if (binding.nameField.text.toString().isEmpty()) {
                binding.nameFieldLayout.isErrorEnabled = true
                binding.nameFieldLayout.error = getString(R.string.name_field_empty)
            } else {
                webSocketSession.sendMessage(
                    "{\"event\":\"connect\",\"name\":\"${binding.nameField.text}\",\"gameId\":$gameId}"
                )
                name = binding.nameField.text.toString()
                binding.gameJoin.visibility = View.GONE
                binding.nameFieldLayout.visibility = View.GONE
                binding.nameText.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i("JoinGameFragment", "Destroying view.")
        webSocketSession.removeObserver(this)
    }

    override fun update(session: Observable, message: Any?) {
        requireActivity().runOnUiThread {
            val pair = message as Pair<*, *>
            val event = pair.first as GameEventType
            var args = ""
            if (pair.second != null) {
                args = pair.second as String
            }
            when (event) {
                GameEventType.CONNECTED -> binding.statusText.text = getString(R.string.status, "CONNECTED")
                GameEventType.CONNECTING -> binding.statusText.text = getString(R.string.status, "CONNECTING...")
                GameEventType.DEBUG_MESSAGE -> binding.statusText.text = getString(R.string.status, args)
                GameEventType.FAIL -> binding.statusText.text = getString(R.string.status,"FAIL! $args")
                GameEventType.STATUS -> {
                    requireActivity().supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<GameFragment>(
                            R.id.game_fragment_container_view,
                            args = bundleOf(
                                "GAME_UID" to gameId,
                                "NAME" to name
                            )
                        )
                    }
                }
                GameEventType.NO_SUCH_GAME -> {
                    binding.gameJoin.visibility = View.VISIBLE
                    binding.nameFieldLayout.visibility = View.VISIBLE
                    binding.nameFieldLayout.isErrorEnabled = true
                    binding.nameFieldLayout.error = "Game doesn't exist"
                    binding.statusText.text = getString(R.string.status, "NO_SUCH_GAME")
                    Snackbar.make(
                        binding.root,
                        "Game with ID $gameId doesn't exist!",
                        NO_SUCH_GAME_SNACKBAR_DURATION)
                        .show()
                }
                GameEventType.NAME_TAKEN -> {
                    binding.gameJoin.visibility = View.VISIBLE
                    binding.nameFieldLayout.visibility = View.VISIBLE
                    binding.nameFieldLayout.isErrorEnabled = true
                    binding.nameFieldLayout.error = "Name taken!"
                    binding.statusText.text = getString(R.string.status, "NAME TAKEN")
                }
                else -> binding.statusText.text = getString(R.string.status, "UNKNOWN")
            }
        }
    }
}
