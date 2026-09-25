package com.sepantartd.shirokhorshid.keyboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.sepantartd.shirokhorshid.R
import com.sepantartd.shirokhorshid.emoji.StickerHelper

class ShiroKhorshidInputMethodService : InputMethodService() {

    private var currentMode: KeyboardMode = KeyboardMode.PERSIAN
    private var previousMode: KeyboardMode = KeyboardMode.PERSIAN
    private var isShifted: Boolean = false
    private var activeEmojiCategory: Int = 0 // 0: Lion & Sun, 1: Smileys, 2: Animals, 3: Food
    private lateinit var rowsContainer: LinearLayout

    private val persianRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ")
    private val persianRow2 = listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ")
    private val persianRow3 = listOf("ظ", "ط", "ز", "ر", "ذ", "د", "پ", "و", "ژ")

    private val englishRow1Lower = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    private val englishRow1Upper = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    private val englishRow2Lower = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    private val englishRow2Upper = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    private val englishRow3Lower = listOf("z", "x", "c", "v", "b", "n", "m")
    private val englishRow3Upper = listOf("Z", "X", "C", "V", "B", "N", "M")

    private val numberRow1 = listOf("۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "۰")
    private val numberRow2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")")
    private val numberRow3 = listOf("*", "\"", "'", ":", ";", "!", "?", "،")

    private val smileyEmojis = listOf("😀", "😃", "😄", "😁", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳")
    private val animalEmojis = listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋")
    private val foodEmojis = listOf("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶", "🌽", "🥕", "🧄", "🧅", "🥔", "🍠")

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null)
        rowsContainer = root.findViewById(R.id.layoutRowsContainer)
        renderKeyboard()
        return root
    }

    private fun renderKeyboard() {
        rowsContainer.removeAllViews()

        when (currentMode) {
            KeyboardMode.PERSIAN -> renderPersianLayout()
            KeyboardMode.ENGLISH -> renderEnglishLayout()
            KeyboardMode.NUMBERS -> renderNumbersLayout()
            KeyboardMode.EMOJI -> renderEmojiLayout()
        }
    }

    private fun renderPersianLayout() {
        addRowOfKeys(persianRow1)
        addRowOfKeys(persianRow2)
        
        val row3Layout = createRowLayout()
        row3Layout.addView(createSpecialButton("؟") { commitText("؟") })
        for (key in persianRow3) {
            row3Layout.addView(createKeyButton(key))
        }
        row3Layout.addView(createSpecialButton("⌫", weight = 1.5f) { handleDelete() })
        rowsContainer.addView(row3Layout)

        renderBottomRow("EN", "?۱۲۳")
    }

    private fun renderEnglishLayout() {
        val row1 = if (isShifted) englishRow1Upper else englishRow1Lower
        val row2 = if (isShifted) englishRow2Upper else englishRow2Lower
        val row3 = if (isShifted) englishRow3Upper else englishRow3Lower

        addRowOfKeys(row1)
        addRowOfKeys(row2)

        val row3Layout = createRowLayout()
        val shiftLabel = if (isShifted) "⇪" else "⇧"
        row3Layout.addView(createSpecialButton(shiftLabel, weight = 1.5f) {
            isShifted = !isShifted
            renderKeyboard()
        })
        for (key in row3) {
            row3Layout.addView(createKeyButton(key))
        }
        row3Layout.addView(createSpecialButton("⌫", weight = 1.5f) { handleDelete() })
        rowsContainer.addView(row3Layout)

        renderBottomRow("FA", "?123")
    }

    private fun renderNumbersLayout() {
        addRowOfKeys(numberRow1)
        addRowOfKeys(numberRow2)

        val row3Layout = createRowLayout()
        for (key in numberRow3) {
            row3Layout.addView(createKeyButton(key))
        }
        row3Layout.addView(createSpecialButton("⌫", weight = 1.5f) { handleDelete() })
        rowsContainer.addView(row3Layout)

        val returnLabel = if (previousMode == KeyboardMode.ENGLISH) "ABC" else "فارسی"
        renderBottomRow(langToggleLabel = "FA/EN", modeToggleLabel = returnLabel)
    }

    private fun renderBottomRow(langToggleLabel: String, modeToggleLabel: String) {
        val bottomRow = createRowLayout()

        bottomRow.addView(createSpecialButton(modeToggleLabel, weight = 1.4f) {
            currentMode = if (currentMode == KeyboardMode.NUMBERS) {
                if (previousMode == KeyboardMode.EMOJI) KeyboardMode.PERSIAN else previousMode
            } else {
                previousMode = currentMode
                KeyboardMode.NUMBERS
            }
            renderKeyboard()
        })

        bottomRow.addView(createSpecialButton(langToggleLabel, weight = 1.2f) {
            currentMode = if (currentMode == KeyboardMode.ENGLISH) {
                KeyboardMode.PERSIAN
            } else {
                KeyboardMode.ENGLISH
            }
            isShifted = false
            renderKeyboard()
        })

        bottomRow.addView(createSpecialButton("😊", weight = 1.2f) {
            previousMode = currentMode
            currentMode = KeyboardMode.EMOJI
            renderKeyboard()
        })

        bottomRow.addView(createSpecialButton("فاصله", weight = 3.5f) {
            commitText(" ")
        })

        bottomRow.addView(createSpecialButton(".", weight = 1f) {
            commitText(".")
        })

        bottomRow.addView(createSpecialButton("↵", weight = 1.4f, bgColor = "#1A73E8", textColor = "#FFFFFF") {
            handleEnter()
        })

        rowsContainer.addView(bottomRow)
    }

    private fun renderEmojiLayout() {
        val categoryBarScrollView = HorizontalScrollView(this)
        val categoryBar = LinearLayout(this)
        categoryBar.orientation = LinearLayout.HORIZONTAL
        categoryBar.setPadding(4, 4, 4, 4)

        val cat0 = createTabButton("🦁 شیر و خورشید", activeEmojiCategory == 0) {
            activeEmojiCategory = 0
            renderKeyboard()
        }
        val cat1 = createTabButton("😀 شکلک‌ها", activeEmojiCategory == 1) {
            activeEmojiCategory = 1
            renderKeyboard()
        }
        val cat2 = createTabButton("🐱 حیوانات", activeEmojiCategory == 2) {
            activeEmojiCategory = 2
            renderKeyboard()
        }
        val cat3 = createTabButton("🍕 خوراکی‌ها", activeEmojiCategory == 3) {
            activeEmojiCategory = 3
            renderKeyboard()
        }

        categoryBar.addView(cat0)
        categoryBar.addView(cat1)
        categoryBar.addView(cat2)
        categoryBar.addView(cat3)
        categoryBarScrollView.addView(categoryBar)
        rowsContainer.addView(categoryBarScrollView)

        when (activeEmojiCategory) {
            0 -> renderLionAndSunEmojiCategory()
            1 -> renderEmojiGrid(smileyEmojis)
            2 -> renderEmojiGrid(animalEmojis)
            3 -> renderEmojiGrid(foodEmojis)
        }

        val emojiBottomRow = createRowLayout()
        emojiBottomRow.addView(createSpecialButton("🔤 کیبورد", weight = 2f) {
            currentMode = if (previousMode == KeyboardMode.EMOJI) KeyboardMode.PERSIAN else previousMode
            renderKeyboard()
        })
        emojiBottomRow.addView(createSpecialButton("فاصله", weight = 3f) {
            commitText(" ")
        })
        emojiBottomRow.addView(createSpecialButton("⌫", weight = 1.5f) {
            handleDelete()
        })
        rowsContainer.addView(emojiBottomRow)
    }

    private fun renderLionAndSunEmojiCategory() {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER
        container.setPadding(16, 16, 16, 16)

        val stickerButton = ImageButton(this)
        stickerButton.setImageResource(R.drawable.ic_lion_and_sun)
        stickerButton.scaleType = ImageView.ScaleType.FIT_CENTER
        stickerButton.setBackgroundColor(Color.TRANSPARENT)

        val params = LinearLayout.LayoutParams(160, 160)
        stickerButton.layoutParams = params

        stickerButton.setOnClickListener {
            onLionAndSunStickerClicked()
        }

        val tvLabel = TextView(this)
        tvLabel.text = "استیکر شیر و خورشید (برای ارسال لمس کنید)"
        tvLabel.setTextColor(Color.parseColor("#CBD5E1"))
        tvLabel.textSize = 12f
        tvLabel.gravity = Gravity.CENTER
        tvLabel.setPadding(0, 8, 0, 0)

        container.addView(stickerButton)
        container.addView(tvLabel)
        rowsContainer.addView(container)
    }

    private fun onLionAndSunStickerClicked() {
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo ?: return
        val uri = StickerHelper.getLionAndSunStickerUri(this) ?: run {
            Toast.makeText(this, "خطا در ایجاد فایل استیکر", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo)
        var isSupported = false
        for (type in mimeTypes) {
            if (ClipDescription.compareMimeTypes(type, "image/png") ||
                ClipDescription.compareMimeTypes(type, "image/*") ||
                ClipDescription.compareMimeTypes(type, "*/*")) {
                isSupported = true
                break
            }
        }

        if (isSupported) {
            val inputContentInfo = InputContentInfoCompat(
                uri,
                ClipDescription("شیر و خورشید", arrayOf("image/png")),
                null
            )
            val flags = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
            val success = InputConnectionCompat.commitContent(ic, editorInfo, inputContentInfo, flags, null)
            if (success) {
                return
            }
        }

        // Fallback: Copy to Clipboard if commitContent fails or is not supported by target app
        copyStickerToClipboard(uri)
    }

    private fun copyStickerToClipboard(uri: Uri) {
        try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newUri(contentResolver, "شیر و خورشید", uri)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this,
                "برنامه مقصد پشتیبانی نمی‌کند؛ تصویر در حافظه کپی شد (آماده Paste)",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "امکان ارسال یا کپی تصویر در این برنامه وجود ندارد", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderEmojiGrid(emojis: List<String>) {
        val itemsPerRow = 6
        var currentRow: LinearLayout? = null

        for ((index, emoji) in emojis.withIndex()) {
            if (index % itemsPerRow == 0) {
                currentRow = createRowLayout()
                rowsContainer.addView(currentRow)
            }
            val btn = createEmojiButton(emoji)
            currentRow?.addView(btn)
        }
    }

    private fun createEmojiButton(emoji: String): Button {
        val btn = Button(this)
        btn.text = emoji
        btn.textSize = 20f
        btn.setBackgroundColor(Color.TRANSPARENT)
        btn.setPadding(0, 0, 0, 0)

        val params = LinearLayout.LayoutParams(
            0,
            120,
            1.0f
        )
        params.setMargins(2, 2, 2, 2)
        btn.layoutParams = params

        btn.setOnClickListener {
            commitText(emoji)
        }
        return btn
    }

    private fun createTabButton(title: String, isSelected: Boolean, onClick: () -> Unit): Button {
        val btn = Button(this)
        btn.text = title
        btn.textSize = 13f
        btn.isAllCaps = false
        btn.setPadding(24, 0, 24, 0)

        if (isSelected) {
            btn.setTextColor(Color.parseColor("#FFFFFF"))
            btn.setBackgroundColor(Color.parseColor("#1A73E8"))
        } else {
            btn.setTextColor(Color.parseColor("#94A3B8"))
            btn.setBackgroundColor(Color.parseColor("#334155"))
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            110
        )
        params.setMargins(4, 4, 4, 4)
        btn.layoutParams = params

        btn.setOnClickListener { onClick() }
        return btn
    }

    private fun addRowOfKeys(keys: List<String>) {
        val rowLayout = createRowLayout()
        for (key in keys) {
            rowLayout.addView(createKeyButton(key))
        }
        rowsContainer.addView(rowLayout)
    }

    private fun createRowLayout(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 3, 0, 3)
        row.layoutParams = params
        return row
    }

    private fun createKeyButton(text: String): Button {
        val btn = Button(this)
        btn.text = text
        btn.textSize = 16f
        btn.setTextColor(Color.parseColor("#F8FAFC"))
        btn.setBackgroundColor(Color.parseColor("#334155"))
        btn.isAllCaps = false
        btn.setPadding(0, 0, 0, 0)

        val params = LinearLayout.LayoutParams(
            0,
            130,
            1.0f
        )
        params.setMargins(2, 2, 2, 2)
        btn.layoutParams = params

        btn.setOnClickListener {
            commitText(text)
            if (isShifted && currentMode == KeyboardMode.ENGLISH) {
                isShifted = false
                renderKeyboard()
            }
        }
        return btn
    }

    private fun createSpecialButton(
        text: String,
        weight: Float = 1.0f,
        bgColor: String = "#475569",
        textColor: String = "#F8FAFC",
        onClick: () -> Unit
    ): Button {
        val btn = Button(this)
        btn.text = text
        btn.textSize = 14f
        btn.setTextColor(Color.parseColor(textColor))
        btn.setBackgroundColor(Color.parseColor(bgColor))
        btn.isAllCaps = false
        btn.setPadding(0, 0, 0, 0)

        val params = LinearLayout.LayoutParams(
            0,
            130,
            weight
        )
        params.setMargins(2, 2, 2, 2)
        btn.layoutParams = params

        btn.setOnClickListener { onClick() }
        return btn
    }

    private fun commitText(text: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
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
}
