package com.vvit.smartcollege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vvit.smartcollege.ui.navigation.RoleNavHost
import com.vvit.smartcollege.ui.theme.PrimaryNavy
import com.vvit.smartcollege.ui.theme.SmartCollegeTheme
import com.vvit.smartcollege.ui.theme.SurfaceLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCollegeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceLight
                ) {
                    RoleNavHost()
                }
            }
        }
    }
}
