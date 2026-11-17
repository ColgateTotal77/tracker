package com.colgateTotal77.tracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.core.enums.OperationId
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.operations.Operations
import com.colgateTotal77.tracker.screens.overview.Overview
import com.colgateTotal77.tracker.screens.products.Products

private val NavigationBarHeight = 64.dp

@Composable
fun NavBar() {
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        R.string.tab_overview,
        R.string.tab_operations,
        R.string.tab_products,
        R.string.tab_analytics,
    )
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.CreditCard,
        Icons.Default.ShoppingCart,
        Icons.Default.Analytics,
    )

    var expendedOperationId by rememberSaveable { mutableStateOf<OperationId?>(null) }

    val dimensions = LocalDimensions.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            val contentModifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)

            when (selectedItemIndex) {
                0 -> Overview(
                    onOperationClick = { id ->
                        expendedOperationId = id
                        selectedItemIndex = 1
                    },
                    modifier = contentModifier
                )

                1 -> Operations(
                    expendedOperationId = expendedOperationId,
                    changeExpendedOperationId = { id ->
                        expendedOperationId = id
                    },
                    modifier = contentModifier
                )

                2 -> Products(modifier = contentModifier)

                3 -> Box(
                    modifier = contentModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.screen_name,
                            stringResource(items[selectedItemIndex])
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = dimensions.screenHorizontalPadding, vertical = dimensions.spacingSmall)
                .shadow(12.dp, RoundedCornerShape(dimensions.cornerRadius))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), RoundedCornerShape(dimensions.cornerRadius)),
        ) {
            items.forEachIndexed { index, item ->
                NavBarItem(
                    selected = selectedItemIndex == index,
                    onClick = { selectedItemIndex = index },
                    icon = { Icon(icons[index], contentDescription = stringResource(item)) },
                    label = {
                        Text(stringResource(item), style = MaterialTheme.typography.labelMedium)
                    },
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    val iconColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
    val textColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .defaultMinSize(minHeight = NavigationBarHeight)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides iconColor) {
                icon()
            }
        }

        CompositionLocalProvider(LocalContentColor provides textColor) {
            label()
        }
    }
}
