package com.colgateTotal77.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.colgateTotal77.tracker.core.ui.theme.Theme
import com.colgateTotal77.tracker.core.ui.AppToastHost
import com.colgateTotal77.tracker.core.ui.theme.ThemeMode
import com.colgateTotal77.tracker.core.workers.exchangeRates.ExchangeRatesWorker
import com.colgateTotal77.tracker.screens.settings.SettingsViewModel
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.settings.value == null }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            Theme(settings?.theme ?: ThemeMode.System) {
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