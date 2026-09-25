package com.sepantartd.shirokhorshid.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.sepantartd.shirokhorshid.R

class ShiroKhorshidInputMethodService : InputMethodService() {

    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null

    private lateinit var mainContainer: LinearLayout
    private lateinit var keyboardLayout: LinearLayout
    private lateinit var emojiLayout: LinearLayout
    private lateinit var stickerLayout: LinearLayout

    private var isPersian = true
    private var isShifted = false
    private var isSymPage1 = true
    private var currentMode = Mode.TEXT

    enum class Mode {
        TEXT, SYMBOLS, EMOJI, STICKERS
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onCreateInputView(): View {
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#1E293B"))
        }

        buildKeyboardViews()
        showMode(Mode.TEXT)

        return mainContainer
    }

    private fun buildKeyboardViews() {
        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(4, 8, 4, 8)
        }

        emojiLayout = createEmojiPanel()
        stickerLayout = createStickerPanel()

        renderTextLayout()
    }

    private fun showMode(mode: Mode) {
        currentMode = mode
        mainContainer.removeAllViews()
        try {
            when (mode) {
                Mode.TEXT -> {
                    renderTextLayout()
                    mainContainer.addView(keyboardLayout)
                }
                Mode.SYMBOLS -> {
                    renderSymbolLayout()
                    mainContainer.addView(keyboardLayout)
                }
                Mode.EMOJI -> {
                    mainContainer.addView(emojiLayout)
                }
                Mode.STICKERS -> {
                    mainContainer.addView(stickerLayout)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderTextLayout() {
        keyboardLayout.removeAllViews()
        val keys = if (isPersian) getPersianKeys() else getEnglishKeys()
        
        for (row in keys) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    120
                )
            }

            for (key in row) {
                val button = createKeyButton(key)
                rowLayout.addView(button)
            }
            keyboardLayout.addView(rowLayout)
        }
        keyboardLayout.addView(createBottomRow())
    }

    private fun renderSymbolLayout() {
        keyboardLayout.removeAllViews()
        val symbolKeys = if (isSymPage1) getSymbolKeysPage1() else getSymbolKeysPage2()
        
        for (row in symbolKeys) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    120
                )
            }

            for (key in row) {
                val button = createKeyButton(key)
                rowLayout.addView(button)
            }
            keyboardLayout.addView(rowLayout)
        }
        keyboardLayout.addView(createSymbolBottomRow())
    }

    private fun createKeyButton(label: String): Button {
        val displayLabel = if (!isPersian && isShifted && label.length == 1) label.uppercase() else label

        return Button(this).apply {
            text = displayLabel
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#334155"))
            
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            params.setMargins(3, 3, 3, 3)
            layoutParams = params
            setPadding(0, 0, 0, 0)

            setOnClickListener {
                playKeyClick()
                handleKeyInput(displayLabel)
            }
        }
    }

    private fun createBottomRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )

            val btnSym = createActionCustomButton("?123", 1.5f, Color.parseColor("#0F172A")) {
                showMode(Mode.SYMBOLS)
            }
            val btnLang = createActionCustomButton(if (isPersian) "FA" else "EN", 1f, Color.parseColor("#0F172A")) {
                isPersian = !isPersian
                renderTextLayout()
            }
            val btnEmoji = createActionCustomButton("😊", 1f, Color.parseColor("#0F172A")) {
                showMode(Mode.EMOJI)
            }
            val btnSpace = createActionCustomButton(if (isPersian) "فاصله" else "Space", 4f, Color.parseColor("#475569")) {
                commitText(" ")
            }
            val btnDel = createActionCustomButton("⌫", 1.5f, Color.parseColor("#DC2626")) {
                handleDelete()
            }
            val btnEnter = createActionCustomButton("↵", 1.5f, Color.parseColor("#2563EB")) {
                handleEnter()
            }

            addView(btnSym)
            addView(btnLang)
            addView(btnEmoji)
            addView(btnSpace)
            addView(btnDel)
            addView(btnEnter)
        }
    }

    private fun createSymbolBottomRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )

            val btnABC = createActionCustomButton("ABC", 2f, Color.parseColor("#0F172A")) {
                showMode(Mode.TEXT)
            }
            val btnPage = createActionCustomButton(if (isSymPage1) "1/2" else "2/2", 2f, Color.parseColor("#0F172A")) {
                isSymPage1 = !isSymPage1
                renderSymbolLayout()
            }
            val btnSpace = createActionCustomButton("Space", 4f, Color.parseColor("#475569")) {
                commitText(" ")
            }
            val btnDel = createActionCustomButton("⌫", 2f, Color.parseColor("#DC2626")) {
                handleDelete()
            }

            addView(btnABC)
            addView(btnPage)
            addView(btnSpace)
            addView(btnDel)
        }
    }

    private fun createActionCustomButton(label: String, weight: Float, bgColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(bgColor)
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight
            )
            params.setMargins(3, 3, 3, 3)
            layoutParams = params
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                playKeyClick()
                onClick()
            }
        }
    }

    private fun createEmojiPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                550
            )
            setBackgroundColor(Color.parseColor("#0F172A"))

            val header = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                val btnBack = Button(context).apply {
                    text = "🔙 کیبورد"
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        playKeyClick()
                        showMode(Mode.TEXT)
                    }
                }
                val btnStickerMode = Button(context).apply {
                    text = "🖼 استیکرها"
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        playKeyClick()
                        showMode(Mode.STICKERS)
                    }
                }
                addView(btnBack)
                addView(btnStickerMode)
            }
            addView(header)

            val emojis = listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "☺️", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘",
                "🇮🇷", "🦁", "☀️", "👑", "🗡️", "❤️", "🤍", "💚", "🔥", "✨", "🎉", "✌️", "✊", "🤝", "🙏", "💯", "👍", "👎", "👏", "💪"
            )

            val grid = GridLayout(context).apply {
                columnCount = 8
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (emoji in emojis) {
                val tv = TextView(context).apply {
                    text = emoji
                    textSize = 22f
                    setPadding(12, 12, 12, 12)
                    setOnClickListener {
                        playKeyClick()
                        commitText(emoji)
                    }
                }
                grid.addView(tv)
            }
            addView(grid)
        }
    }

    private fun createStickerPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                550
            )
            setBackgroundColor(Color.parseColor("#0F172A"))

            val btnBack = Button(context).apply {
                text = "🔙 بازگشت به کیبورد"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    playKeyClick()
                    showMode(Mode.TEXT)
                }
            }
            addView(btnBack)

            val infoText = TextView(context).apply {
                text = "بخش استیکرهای نمادین شیر و خورشید فعال است"
                setTextColor(Color.LTGRAY)
                textSize = 15f
                setPadding(24, 24, 24, 24)
            }
            addView(infoText)
        }
    }

    private fun handleKeyInput(key: String) {
        when (key) {
            "⇧" -> {
                isShifted = !isShifted
                renderTextLayout()
            }
            "نیم‌فاصله" -> commitText("‌")
            else -> commitText(key)
        }
    }

    private fun commitText(text: String) {
        val ic = currentInputConnection
        ic?.commitText(text, 1)
    }

    private fun handleDelete() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (selectedText.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun playKeyClick() {
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
            vibrator?.let {
                if (it.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(15)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPersianKeys(): List<List<String>> {
        return listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ"),
            listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ"),
            listOf("ظ", "ط", "ز", "ر", "ذ", "د", "پ", "و", "نیم‌فاصله", "؛")
        )
    }

    private fun getEnglishKeys(): List<List<String>> {
        return listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("⇧", "z", "x", "c", "v", "b", "n", "m", ".")
        )
    }

    private fun getSymbolKeysPage1(): List<List<String>> {
        return listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
            listOf("*", "\"", "'", ":", ";", "!", "?", "،", "«", "»")
        )
    }

    private fun getSymbolKeysPage2(): List<List<String>> {
        return listOf(
            listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆"),
            listOf("£", "¢", "€", "¥", "^", "=", "{", "}", "[", "]"),
            listOf("°", "\\", "©", "®", "™", "℅", "<", ">", "⌫")
        )
    }
}

private fun CharSequence?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}
