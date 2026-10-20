package com.colgateTotal77.tracker.core.workers.exchangeRates

import com.colgateTotal77.tracker.screens.overview.ExchangeRatesPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONArray

private val client = OkHttpClient()

suspend fun fetchExchangeRates(): ExchangeRatesPreferences {
    return withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            val body = response.body?.string() ?: throw IOException("Empty response")

            val entries = JSONArray(body)
            val rates = (0 until entries.length())
                .map { entries.getJSONObject(it) }
                .associate { it.getString("cc") to it.getDouble("rate") }

            ExchangeRatesPreferences(
                usdUahExchangeRate = rates["USD"] ?: throw IOException("Missing USD rate"),
                eurUahExchangeRate = rates["EUR"] ?: throw IOException("Missing EUR rate"),
            )
        }
    }
}