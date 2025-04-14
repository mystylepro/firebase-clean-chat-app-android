package com.app.chat.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import com.app.chat.R


fun NavController.navigateAndClearBackStack(
    @IdRes destinationId: Int,
    args: Bundle? = null,
    @IdRes popUpToId: Int = R.id.nav_graph,
    inclusive: Boolean = true,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    val options = navOptions {
        popUpTo(popUpToId) { this.inclusive = inclusive }
        builder?.invoke(this)
    }
    navigate(destinationId, args, options)
}

fun NavController.navigateWithPopUp(
    directions: NavDirections,
    @IdRes popUpTo: Int,
    inclusive: Boolean = true
) {
    val options = navOptions {
        popUpTo(popUpTo) { this.inclusive = inclusive }
    }
    navigate(directions, options)
}