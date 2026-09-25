package com.sepantartd.shirokhorshid

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sepantartd.shirokhorshid.settings.SettingsManager

class MainActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsManager = SettingsManager(this)

        val btnEnableKeyboard = findViewById<Button>(R.id.btnEnableKeyboard)
        val btnSelectKeyboard = findViewById<Button>(R.id.btnSelectKeyboard)
        val switchHaptic = findViewById<SwitchMaterial>(R.id.switchHaptic)
        val switchSound = findViewById<SwitchMaterial>(R.id.switchSound)

        btnEnableKeyboard.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        btnSelectKeyboard.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        switchHaptic.isChecked = settingsManager.isHapticEnabled
        switchSound.isChecked = settingsManager.isSoundEnabled

        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.isHapticEnabled = isChecked
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.isSoundEnabled = isChecked
        }
    }
}
