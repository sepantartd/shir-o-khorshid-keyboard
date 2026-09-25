package com.sepantartd.shirokhorshid.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import com.sepantartd.shirokhorshid.R

class ShiroKhorshidInputMethodService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        val btnKeyA = view.findViewById<Button>(R.id.btnKeyA)
        val btnKeyB = view.findViewById<Button>(R.id.btnKeyB)
        val btnKeyC = view.findViewById<Button>(R.id.btnKeyC)
        val btnKeySpace = view.findViewById<Button>(R.id.btnKeySpace)
        val btnKeyDelete = view.findViewById<Button>(R.id.btnKeyDelete)
        val btnKeyEnter = view.findViewById<Button>(R.id.btnKeyEnter)

        btnKeyA.setOnClickListener { commitText("ا") }
        btnKeyB.setOnClickListener { commitText("ب") }
        btnKeyC.setOnClickListener { commitText("پ") }
        btnKeySpace.setOnClickListener { commitText(" ") }

        btnKeyDelete.setOnClickListener {
            val ic = currentInputConnection ?: return@setOnClickListener
            val selectedText = ic.getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
                ic.deleteSurroundingText(1, 0)
            } else {
                ic.commitText("", 1)
            }
        }

        btnKeyEnter.setOnClickListener {
            val ic = currentInputConnection ?: return@setOnClickListener
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }

        return view
    }

    private fun commitText(text: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
    }
}
