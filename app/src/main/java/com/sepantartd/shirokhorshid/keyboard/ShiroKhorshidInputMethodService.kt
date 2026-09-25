package com.sepantartd.shirokhorshid.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            setPadding(8, 12, 8, 12)
        }

        emojiLayout = createEmojiPanel()
        stickerLayout = createStickerPanel()

        renderTextLayout()
    }

    private fun showMode(mode: Mode) {
        currentMode = mode
        mainContainer.removeAllViews()

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
    }

    private fun renderTextLayout() {
        keyboardLayout.removeAllViews()

        val keys = if (isPersian) getPersianKeys() else getEnglishKeys()
        for (row in keys) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                weightSum = row.size.toFloat()
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
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                weightSum = row.size.toFloat()
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
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#334155"))
            
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params.setMargins(4, 6, 4, 6)
            layoutParams = params

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
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val btnSym = Button(context).apply {
                text = "?123"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#0F172A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    showMode(Mode.SYMBOLS)
                }
            }

            val btnLang = Button(context).apply {
                text = if (isPersian) "FA" else "EN"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#0F172A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    isPersian = !isPersian
                    renderTextLayout()
                }
            }

            val btnEmoji = Button(context).apply {
                text = "😊"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#0F172A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    showMode(Mode.EMOJI)
                }
            }

            val btnSpace = Button(context).apply {
                text = if (isPersian) "فاصله" else "Space"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#475569"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    commitText(" ")
                }
            }

            val btnDel = Button(context).apply {
                text = "⌫"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#DC2626"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    handleDelete()
                }
            }

            val btnEnter = Button(context).apply {
                text = "↵"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#2563EB"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    handleEnter()
                }
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
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val btnABC = Button(context).apply {
                text = "ABC"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#0F172A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    showMode(Mode.TEXT)
                }
            }

            val btnPage = Button(context).apply {
                text = if (isSymPage1) "1/2" else "2/2"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#0F172A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    isSymPage1 = !isSymPage1
                    renderSymbolLayout()
                }
            }

            val btnSpace = Button(context).apply {
                text = "Space"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#475569"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    commitText(" ")
                }
            }

            val btnDel = Button(context).apply {
                text = "⌫"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#DC2626"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply { setMargins(4, 6, 4, 6) }
                setOnClickListener {
                    playKeyClick()
                    handleDelete()
                }
            }

            addView(btnABC)
            addView(btnPage)
            addView(btnSpace)
            addView(btnDel)
        }
    }

    private fun createEmojiPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
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
                    textSize = 24f
                    setPadding(16, 16, 16, 16)
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
                600
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
                text = "بخش استیکرهای نمادین شیر و خورشید"
                setTextColor(Color.LTGRAY)
                textSize = 16f
                setPadding(32, 32, 32, 32)
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
        currentInputConnection?.commitText(text, 1)
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

    fun playKeyClick() {
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        performHapticFeedback()
    }

    private fun performHapticFeedback() {
        vibrator?.let {
            if (it.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(20)
                }
            }
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
