package com.sepantartd.shirokhorshid.keyboard

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.sepantartd.shirokhorshid.R

class ShiroKhorshidInputMethodService : InputMethodService() {

    private var currentMode: KeyboardMode = KeyboardMode.PERSIAN
    private var isShifted: Boolean = false
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

        val returnLabel = if (currentMode == KeyboardMode.ENGLISH) "ABC" else "فارسی"
        renderBottomRow(langToggleLabel = "FA/EN", modeToggleLabel = returnLabel)
    }

    private fun renderBottomRow(langToggleLabel: String, modeToggleLabel: String) {
        val bottomRow = createRowLayout()

        bottomRow.addView(createSpecialButton(modeToggleLabel, weight = 1.5f) {
            currentMode = if (currentMode == KeyboardMode.NUMBERS) {
                KeyboardMode.PERSIAN
            } else {
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

        bottomRow.addView(createSpecialButton("فاصله", weight = 4f) {
            commitText(" ")
        })

        bottomRow.addView(createSpecialButton(".", weight = 1f) {
            commitText(".")
        })

        bottomRow.addView(createSpecialButton("↵", weight = 1.5f, bgColor = "#1A73E8", textColor = "#FFFFFF") {
            handleEnter()
        })

        rowsContainer.addView(bottomRow)
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
