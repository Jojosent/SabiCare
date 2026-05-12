package com.example.sabicare_j.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Lightweight "on resume" effect — invokes [onResume] every time the
 * surrounding composable's lifecycle reaches ON_RESUME.  Lets screens that
 * sit in the back stack refresh their data when the user returns to them.
 */
@Composable
fun OnResumeEffect(onResume: () -> Unit) {
    val owner: LifecycleOwner = LocalLifecycleOwner.current
    val current by rememberUpdatedState(onResume)
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) current()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
