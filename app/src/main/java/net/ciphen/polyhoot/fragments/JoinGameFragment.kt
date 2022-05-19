package net.ciphen.polyhoot.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import net.ciphen.polyhoot.R
import net.ciphen.polyhoot.activity.GameActivity
import net.ciphen.polyhoot.databinding.FragmentJoinGameBinding
import net.ciphen.polyhoot.databinding.GameActivityBinding
import net.ciphen.polyhoot.game.WebSocketSession
import net.ciphen.polyhoot.game.event.GameEventType
import net.ciphen.polyhoot.patterns.observer.Observable
import net.ciphen.polyhoot.patterns.observer.Observer
import kotlin.properties.Delegates

class JoinGameFragment : Fragment(), Observer {
    private var _binding: FragmentJoinGameBinding? = null
    private val binding get() = _binding!!
    private var webSocketSession = GameActivity.getInstance().webSocketSession
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
            webSocketSession.sendMessage(
                "{\"event\":\"connect\",\"name\":\"${binding.nameField.text}\",\"gameId\":$gameId}"
            )
            name = binding.nameField.text.toString()
            binding.gameJoin.visibility = View.GONE
            binding.nameFieldLayout.visibility = View.GONE
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
            val session = session as WebSocketSession
            when (event) {
                GameEventType.CONNECTED -> binding.statusText.text = getString(R.string.status, "CONNECTED")
                GameEventType.CONNECTING -> binding.statusText.text = getString(R.string.status, "CONNECTING...")
                GameEventType.DEBUG_MESSAGE -> binding.statusText.text = getString(R.string.status, args)
                GameEventType.FAIL -> binding.statusText.text = getString(R.string.status,"FAIL! $args")
                GameEventType.STATUS -> {
                    requireActivity().supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<GameFragment>(R.id.game_fragment_container_view, args = bundleOf("GAME_UID" to gameId, "NAME" to name))
                    }
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