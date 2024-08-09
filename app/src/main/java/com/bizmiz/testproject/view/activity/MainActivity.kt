package com.bizmiz.testproject.view.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import cafe.adriel.voyager.navigator.Navigator
import com.bizmiz.testproject.view.compose.HomeScreen
import com.bizmiz.testproject.view.theme.TestProjectTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            TestProjectTheme {
                Surface {
                    Navigator(HomeScreen)
                }
            }
        }
    }
}