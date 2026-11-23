package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.ServiceWorkerClient
import android.webkit.ServiceWorkerController
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import kotlin.time.Duration.Companion.milliseconds

private const val ASK_USER_AFTER_MS = 15_000L
private const val GIVE_UP_AFTER_MS = 90_000L
private val WIDGET_SIZE = DpSize(300.dp, 65.dp)
private val PAGE_SIZE = DpSize(360.dp, 600.dp)

private const val WIDGET_ONLY_JS =
    "var s=document.createElement('style');" +
        "s.textContent='body *{visibility:hidden!important}" +
        "cb-captcha,cb-captcha *{visibility:visible!important}" +
        "cb-captcha{position:fixed!important;top:0;left:0;z-index:2147483647}';" +
        "document.head.appendChild(s);"

class CaptchaGate {
    var params by mutableStateOf<FiscalQrParams?>(null)
        private set
    var needsUser by mutableStateOf(false)
    private var deferred: CompletableDeferred<String?>? = null

    suspend fun request(params: FiscalQrParams): String? = withContext(Dispatchers.Main) {
        val pending = CompletableDeferred<String?>()
        deferred = pending
        this@CaptchaGate.params = params
        pending.await()
    }

    fun resolve(token: String?) {
        deferred?.complete(token)
        deferred = null
        params = null
        needsUser = false
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FiscalCaptcha(gate: CaptchaGate, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                    interceptToken(request, gate, context)

                override fun onPageFinished(view: WebView, url: String) = view.evaluateJavascript(WIDGET_ONLY_JS, null)
            }
            ServiceWorkerController.getInstance().setServiceWorkerClient(object : ServiceWorkerClient() {
                override fun shouldInterceptRequest(request: WebResourceRequest) = interceptToken(request, gate, context)
            })
            loadUrl(CHECK_PAGE)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ServiceWorkerController.getInstance().setServiceWorkerClient(null)
            webView.destroy()
        }
    }

    LaunchedEffect(gate.params) {
        val params = gate.params ?: return@LaunchedEffect
        webView.loadUrl(TaxApi.checkPageUrl(params))
        delay(ASK_USER_AFTER_MS.milliseconds)
        gate.needsUser = true
        delay((GIVE_UP_AFTER_MS - ASK_USER_AFTER_MS).milliseconds)
        gate.resolve(null)
    }

    val dimensions = LocalDimensions.current
    Card(modifier.padding(dimensions.spacingMedium).zIndex(if (gate.needsUser) 1f else -1f)) {
        Column(Modifier.padding(dimensions.contentPadding), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.captcha_hint), style = MaterialTheme.typography.bodyMedium)
            Box(Modifier.padding(top = dimensions.spacingSmall).size(WIDGET_SIZE).clipToBounds()) {
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).requiredSize(PAGE_SIZE),
                )
            }
            TextButton(onClick = { gate.resolve(null) }) { Text(stringResource(R.string.cancel)) }
        }
    }
}

private fun interceptToken(request: WebResourceRequest, gate: CaptchaGate, context: Context): WebResourceResponse? {
    if (request.url.path?.endsWith("chkAllWeb") != true) return null
    val token = request.url.getQueryParameter("captcha")?.takeIf { it.isNotBlank() } ?: return null
    ContextCompat.getMainExecutor(context).execute { gate.resolve(token) }
    return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
}
