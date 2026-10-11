package com.colgateTotal77.tracker.screens.settings

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.colgateTotal77.tracker.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.colgateTotal77.tracker.core.enums.Language
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun Settings(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    BackHandler(onBack = onBack)

    val dimensions = LocalDimensions.current
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Log.d("Settings", settings.toString())

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.screenPadding)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            ) {
                CustomIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(dimensions.leadingIconSize),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.tab_overview),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.titleLarge)
            }

            settings?.let { settings ->
                Dropdown(
                    items = Language.entries,
                    selected = settings.language,
                    onSelect = { language ->
                        language?.let {
                            viewModel.updateSettings(settings.copy(language = it))
                            viewModel.applyLanguage(it)
                        }
                    },
                    itemText = { context.getString(it.labelRes) },
                    itemName = stringResource(R.string.language),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
