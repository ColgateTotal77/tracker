package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.colgateTotal77.tracker.R
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "FiscalCaptcha"

private const val WEB_VIEW_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Mobile Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FiscalCaptchaDialog(
    params: FiscalQrParams,
    onToken: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val url = remember(params) { TaxApi.checkPageUrl(params) }
    val finished = remember { AtomicBoolean(false) }

    fun finish(token: String?) {
        if (!finished.compareAndSet(false, true)) return
        if (token == null) onCancel() else onToken(token)
    }

    LaunchedEffect(Unit) {
        delay(90_000)
        finish(null)
    }

    Dialog(
        onDismissRequest = { finish(null) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 3.dp,
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(stringResource(R.string.captcha_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.captcha_hint),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                AndroidView(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.userAgentString = WEB_VIEW_UA
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                                    Log.d(TAG, "console: ${message.message()}")
                                    return true
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(
                                    view: WebView,
                                    request: WebResourceRequest,
                                ): WebResourceResponse? {
                                    val token = request.url.getQueryParameter("captcha")
                                    if (token.isNullOrBlank()) return null
                                    if (!request.url.path.orEmpty().endsWith("chkAllWeb")) return null
                                    Log.d(TAG, "captcha token captured")
                                    view.post { finish(token) }
                                    return WebResourceResponse(
                                        "text/plain",
                                        "UTF-8",
                                        ByteArrayInputStream(ByteArray(0)),
                                    )
                                }

                                override fun onPageFinished(view: WebView, url: String) {
                                    view.evaluateJavascript(
                                        "(function(){" +
                                            "var s=document.createElement('style');" +
                                            "s.textContent='p-toast,.p-toast,.p-toast-message,.red_row" +
                                            "{display:none!important}';" +
                                            "document.head.appendChild(s);" +
                                            "var n=0,t=setInterval(function(){" +
                                            "var e=document.querySelector('cb-captcha');" +
                                            "if(e){e.scrollIntoView({block:'center'});clearInterval(t);}" +
                                            "if(++n>20)clearInterval(t);},500);})()",
                                        null,
                                    )
                                }

                                override fun onReceivedError(
                                    view: WebView,
                                    request: WebResourceRequest,
                                    error: WebResourceError,
                                ) {
                                    Log.e(TAG, "webview error ${error.errorCode}: ${error.description}")
                                }
                            }
                            Log.d(TAG, "loading $url")
                            loadUrl(url)
                        }
                    },
                    onRelease = { it.destroy() },
                )
                TextButton(onClick = { finish(null) }, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
