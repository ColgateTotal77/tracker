package com.colgateTotal77.tracker.core.workers.exchangeRates

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.colgateTotal77.tracker.TrackerApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONException
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExchangeRatesWorker (
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = mutex.withLock {
        val app = applicationContext as TrackerApplication
        val preferences = app.overviewPreferencesRepository
        val saved = preferences.exchangeRateFlow.first()

        val now = System.currentTimeMillis()
        val refreshInterval = TimeUnit.MINUTES.toMillis(15)

        if (saved.updatedAt > 0L && now - saved.updatedAt in 0L until refreshInterval) {
            return@withLock Result.success()
        }

        try {
            val rates = fetchExchangeRates()
            preferences.updateExchangeRatesPreferences(rates)
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: JSONException) {
            Result.failure()
        }
    }

    companion object {
        private val mutex = Mutex()
    }
}