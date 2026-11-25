package com.doomscrolljail.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.doomscrolljail.app.data.BlockerPrefs
import com.doomscrolljail.app.ui.DashboardScreen
import com.doomscrolljail.app.ui.theme.DoomscrollJailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = BlockerPrefs(applicationContext)
        setContent {
            DoomscrollJailTheme {
                DashboardScreen(prefs = prefs)
            }
        }
    }
}
