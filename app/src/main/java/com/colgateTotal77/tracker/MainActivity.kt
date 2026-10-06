package com.colgateTotal77.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.colgateTotal77.tracker.core.ui.theme.Theme
import com.colgateTotal77.tracker.core.ui.AppToastHost

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
}