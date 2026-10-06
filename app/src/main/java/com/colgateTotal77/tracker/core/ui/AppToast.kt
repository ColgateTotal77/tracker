package com.colgateTotal77.tracker.core.ui

import android.app.Dialog
import android.graphics.Color
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.colgateTotal77.tracker.core.ui.theme.dimensions
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class ToastType { Default, Error }

object AppToast {
    internal data class Message(val text: String?, val type: ToastType, val id: Long, @param:StringRes val resource: Int? = null)
    private var nextId = 0L
    internal var message by mutableStateOf<Message?>(null)
        private set

    fun show(message: String, type: ToastType = ToastType.Default) {
        this.message = Message(message, type, nextId++)
    }

    fun show(@StringRes message: Int, type: ToastType = ToastType.Default) {
        this.message = Message(null, type, nextId++, message)
    }

    fun hide() {
        message = null
    }
}

@Composable
fun AppToastHost() {
    val message = AppToast.message ?: return
    val appView = LocalView.current
    val context = LocalContext.current
    val composition = rememberCompositionContext()

    DisposableEffect(message.id) {
        val dialog = Dialog(context)
        val window = requireNotNull(dialog.window)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        )
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setViewTreeLifecycleOwner(appView.findViewTreeLifecycleOwner())
        window.decorView.setViewTreeSavedStateRegistryOwner(appView.findViewTreeSavedStateRegistryOwner())
        val content = ComposeView(context).apply {
            setParentCompositionContext(composition)
            setContent { ToastContent(message) }
        }
        dialog.setContentView(content)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.show()
        onDispose {
            dialog.dismiss()
            content.disposeComposition()
        }
    }
}

@Composable
private fun ToastContent(message: AppToast.Message) {
    val dimensions = MaterialTheme.dimensions
    val accessibility = LocalAccessibilityManager.current
    var visible by remember { mutableStateOf(false) }
    val error = message.type == ToastType.Error

    LaunchedEffect(message.id) {
        visible = true
        delay((accessibility?.calculateRecommendedTimeoutMillis(4000L, containsText = true) ?: 4000L).milliseconds)
        visible = false
        delay(250.milliseconds)
        if (AppToast.message?.id == message.id) AppToast.hide()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = dimensions.screenPadding)
            .padding(top = 64.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally(tween(250)) { it } + fadeOut(tween(250)),
        ) {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerRadius),
                color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.inverseSurface,
                contentColor = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.inverseOnSurface,
                shadowElevation = dimensions.elementSpacing,
                modifier = Modifier
                    .widthIn(max = (LocalConfiguration.current.screenWidthDp * 0.8f).dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Text(
                    text = message.resource?.let { stringResource(it) } ?: message.text.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(dimensions.contentPadding),
                )
            }
        }
    }
}
