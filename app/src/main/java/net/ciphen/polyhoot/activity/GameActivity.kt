package net.ciphen.polyhoot.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import net.ciphen.polyhoot.R
import net.ciphen.polyhoot.databinding.GameActivityBinding
import net.ciphen.polyhoot.fragments.JoinGameFragment
import net.ciphen.polyhoot.game.WebSocketSession

class GameActivity: AppCompatActivity() {
    private lateinit var binding: GameActivityBinding
    private lateinit var webSocketSession: WebSocketSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webSocketSession = WebSocketSession.getInstance()
        webSocketSession.openWebSocket(getString(R.string.websocket_url))
        Log.i("GameActivity", "Opened WebSocket.")
        supportFragmentManager.commit {
            val bundle = bundleOf(
                "GAME_UID" to intent.extras!!.get("GAME_UID")
            )
            setReorderingAllowed(true)
            add<JoinGameFragment>(R.id.game_fragment_container_view, args = bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketSession.endSession()
    }
}