package com.colgateTotal77.tracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.dashboard.Dashboard
import com.colgateTotal77.tracker.screens.products.Products

private val NavigationBarHeight = 80.dp
private val IndicatorWidth = 64.dp
private val IndicatorHeight = 32.dp

@Composable
fun NavBar() {
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val items = listOf("Dashboard", "Products", "Portfolio", "Analytics")
    val icons = listOf(Icons.Default.Home, Icons.Default.ShoppingCart, Icons.Default.Folder, Icons.Default.Analytics)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavBarItem(
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = {
                            Text(item, style = MaterialTheme.typography.labelMedium)
                        },
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedItemIndex) {
            0 -> Dashboard(modifier = Modifier.padding(paddingValues))
            1 -> Products(modifier = Modifier.padding(paddingValues))
            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "${items[selectedItemIndex]} Screen")
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
    val dimensions = LocalDimensions.current
    val colors = NavigationBarItemDefaults.colors()

    val iconColor by animateColorAsState(
        targetValue = if (selected) colors.selectedIconColor else colors.unselectedIconColor
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.selectedTextColor else colors.unselectedTextColor
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
        Box(
            modifier = Modifier
                .padding(top = dimensions.elementSpacing)
                .size(width = IndicatorWidth, height = IndicatorHeight)
                .clip(RoundedCornerShape(dimensions.cornerRadius))
                .background(if (selected) colors.selectedIndicatorColor else Color.Transparent)
                .clickable(
                    onClick = onClick,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    role = Role.Tab,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides iconColor) {
                icon()
            }
        }

        Spacer(modifier = Modifier.height(dimensions.elementSpacing))

        CompositionLocalProvider(LocalContentColor provides textColor) {
            label()
        }
    }
}
