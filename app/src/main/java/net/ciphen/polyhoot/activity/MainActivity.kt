package net.ciphen.polyhoot.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import net.ciphen.polyhoot.R
import net.ciphen.polyhoot.databinding.ActivityMainBinding

private const val MAX_GAME_ID_LENGTH = 6

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gameUidEnter.setOnClickListener {
            if (binding.gameUidField.text!!.length != MAX_GAME_ID_LENGTH) {
                binding.gameUidFieldLayout.error = getString(R.string.game_id_error_text)
                binding.gameUidFieldLayout.isErrorEnabled = true
            } else {
                binding.gameUidFieldLayout.isErrorEnabled = false
                val intent = Intent(this, GameActivity::class.java).apply {
                    putExtra("GAME_UID", binding.gameUidField.text!!.toString().toInt())
                }
                startActivity(intent)
            }
        }
    }
}
