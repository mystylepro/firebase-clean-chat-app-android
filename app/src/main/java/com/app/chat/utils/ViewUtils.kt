package com.app.chat.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

fun View.applyKeyboardInsetAsMargin() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (lp != null) {
            lp.bottomMargin = ime.bottom
            view.layoutParams = lp
            view.requestLayout() // 🔥 Without this, changes won't reflect!
        }
        insets
    }
}




