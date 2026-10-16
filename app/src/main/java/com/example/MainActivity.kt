package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.HisaabApp
import com.example.ui.security.SecurityManager
import com.example.ui.theme.HisaabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val securityManager = SecurityManager.getInstance(applicationContext)
        securityManager.applyFlagSecure(this)
        setContent {
            HisaabTheme {
                HisaabApp()
            }
        }
    }
}
