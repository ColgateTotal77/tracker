package com.colgateTotal77.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.colgateTotal77.tracker.core.ui.theme.Theme
import com.colgateTotal77.tracker.core.ui.AppToastHost
import com.colgateTotal77.tracker.core.workers.exchangeRates.ExchangeRatesWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                NavBar()
                AppToastHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val request = OneTimeWorkRequestBuilder<ExchangeRatesWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS,
            )
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "exchange-rates-on-open",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}