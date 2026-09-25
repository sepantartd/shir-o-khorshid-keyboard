package com.sepantartd.shirokhorshid.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.FrameLayout

class ShiroKhorshidInputMethodService : InputMethodService() {

    override fun onCreateInputView(): View {
        val container = FrameLayout(this)
        return container
    }
}
