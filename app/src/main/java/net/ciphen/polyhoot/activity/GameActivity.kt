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
    companion object {
        private var INSTANCE: GameActivity? = null

        fun getInstance(): GameActivity {
            if (INSTANCE == null) {
                throw IllegalStateException("Called getInstance on activity that wasn't created yet!")
            } else {
                return INSTANCE!!
            }
        }
    }
    private lateinit var binding: GameActivityBinding
    lateinit var webSocketSession: WebSocketSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        INSTANCE = this
        binding = GameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webSocketSession = WebSocketSession()
        webSocketSession.openWebSocket()
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