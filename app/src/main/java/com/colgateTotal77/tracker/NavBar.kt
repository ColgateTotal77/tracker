package com.colgateTotal77.tracker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.screens.dashboard.Dashboard
import com.colgateTotal77.tracker.screens.products.Products

@Composable
fun NavBar() {
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val items = listOf("Dashboard", "Products", "Portfolio", "Analytics")
    val icons = listOf(Icons.Default.Home, Icons.Default.ShoppingCart, Icons.Default.Folder, Icons.Default.Analytics)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItemIndex == index,
                        onClick = { selectedItemIndex = index }
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