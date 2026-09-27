package com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal

import android.util.Base64
import android.util.Log
import com.colgateTotal77.tracker.core.database.transaction.TransactionDraft
import com.colgateTotal77.tracker.core.database.transaction.toDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "TaxApi"
private const val CHECK_ENDPOINT = "https://cabinet.tax.gov.ua/ws/api_public/rro/chkAllWeb"
private const val CHECK_TYPE = "3"

data class FiscalQrParams(
    val date: String,
    val time: String?,
    val id: String,
    val sm: String,
    val fn: String,
) {
    val apiDateTime: String
        get() {
            val d = date.filter(Char::isDigit)
            val t = time.orEmpty().filter(Char::isDigit)
            return "${d.take(4)}-${d.substring(4, 6)}-${d.substring(6, 8)} " +
                buildString {
                    append(t.take(2))
                    append(':')
                    if (t.length >= 4) append(t.substring(2, 4)) else append("00")
                    append(':')
                    if (t.length >= 6) append(t.substring(4, 6)) else append("00")
                }
        }
}

object TaxApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun parseQrLink(qrLink: String): FiscalQrParams? {
        val queryIndex = qrLink.indexOf('?')
        if (queryIndex == -1) return null

        val params = mutableMapOf<String, String>()
        qrLink.substring(queryIndex + 1).split('&').forEach { pair ->
            val eq = pair.indexOf('=')
            if (eq > 0) {
                val key = URLDecoder.decode(pair.substring(0, eq), "UTF-8")
                val value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8")
                params[key] = value
            }
        }

        val date = params["date"] ?: return null
        val time = params["time"] ?: return null
        val id = params["id"] ?: return null
        val sm = params["sm"] ?: return null
        val fn = params["fn"] ?: return null
        return FiscalQrParams(date = date, time = time, id = id, sm = sm, fn = fn)
    }

    private fun buildRequestUrl(params: FiscalQrParams, captcha: String?): HttpUrl =
        CHECK_ENDPOINT.toHttpUrl().newBuilder()
            .addQueryParameter("date", params.apiDateTime)
            .addQueryParameter("type", CHECK_TYPE)
            .addQueryParameter("id", params.id)
            .apply {
                if (!captcha.isNullOrBlank()) addQueryParameter("captcha", captcha)
            }
            .addQueryParameter("fn", params.fn)
            .addQueryParameter("sm", params.sm)
            .build()

    suspend fun fetchCheckXml(params: FiscalQrParams, captcha: String?): Pair<String, ByteArray> {
        val request = Request.Builder()
            .url(buildRequestUrl(params, captcha))
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36",
            )
            .get()
            .build()

        var lastError: IOException? = null
        repeat(3) { attempt ->
            if (attempt > 0) {
                Log.w(TAG, "retry attempt ${attempt + 1} after error: $lastError")
                delay((1000L * attempt).milliseconds)
            }
            try {
                val (body, xmlBytes) = fetchOnce(request)
                return body to xmlBytes
            } catch (e: IOException) {
                if (e.message?.contains("Tax API HTTP 4") == true ||
                    e.message?.contains("Tax API:") == true ||
                    e.message?.contains("Check not found") == true
                ) throw e
                lastError = e
            }
        }
        throw IOException("Tax API unreachable after 3 attempts", lastError)
    }

    private suspend fun fetchOnce(request: Request): Pair<String, ByteArray> = withContext(Dispatchers.IO) {
        client.newBuilder().build().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Tax API HTTP ${response.code}")

            val body = response.body?.string().orEmpty()
            Log.d(TAG, "checkXml response: ${body.take(2000)}")

            val json = JSONObject(body)

            fun optField(name: String) = json.optString(name).takeIf { it.isNotBlank() && it != "null" }

            val resultCode = optField("resultCode")
            val resultText = optField("resultText")

            val captchaUrl = optField("captchaUrl")
            if (captchaUrl != null && resultCode == null) throw IOException("Tax API: captcha required: $captchaUrl")
            if (resultCode != null && resultCode != "0") throw IOException("Tax API error code=$resultCode text=$resultText")

            val apiError = optField("error_description")
            if (apiError != null) throw IOException("Check not found in the tax archive (likely expired): $apiError")

            val checkXml = optField("checkXml") ?: optField("check")
                ?: throw IOException("Tax API: missing checkXml, body=${body.take(500)}")

            body to Base64.decode(checkXml, Base64.DEFAULT)
        }
    }

    suspend fun fetchFiscalCheck(qrLink: String, captcha: String? = null): FetchedCheck =
        withContext(Dispatchers.IO) {
            val params = requireNotNull(parseQrLink(qrLink)) { "Invalid QR link: $qrLink" }
            val (responseBody, xmlBytes) = fetchCheckXml(params, captcha)
            val check = CheckXmlParser.parse(xmlBytes)

            val json = JSONObject(responseBody)
            val visual = json.optString("check")
                .takeIf { it.isNotBlank() }
                ?.let { String(Base64.decode(it, Base64.DEFAULT), Charsets.UTF_8) }
            val rawPayload = JSONObject().apply {
                put("xml", String(xmlBytes, CP1251).trim())
                if (visual != null) put("visual", visual)
            }.toString()

            FetchedCheck(check = check, rawPayload = rawPayload)
        }

    private val CP1251 = Charset.forName("windows-1251")
}

data class FetchedCheck(
    val check: FiscalCheck,
    val rawPayload: String,
)

fun FetchedCheck.toDraft(): TransactionDraft =
    check.toDraft(rawFiscalPayload = rawPayload)