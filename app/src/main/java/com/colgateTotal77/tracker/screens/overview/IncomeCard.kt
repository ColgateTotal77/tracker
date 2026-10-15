package com.colgateTotal77.tracker.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.formatTimestamp
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun IncomeCard(
    income: IncomeEntity,
    onClick: () -> Unit
) {
    val dimensions = LocalDimensions.current

    CardWrapper(contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimensions.cornerRadius))
                .clickable { onClick() }
                .padding(dimensions.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.leadingIconSize)
                    .clip(RoundedCornerShape(dimensions.cornerRadius))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Text(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom),
                text = formatTimestamp(income.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "+${formatMoney(income.amountMinor)} ${income.currency.name}",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
