//
//  Copyright (c) 2024 Andrea Bondavalli. All rights reserved.
//
package com.bondagit.aes67player

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bondagit.aes67player.ui.Aes67PlayerApp
import com.bondagit.aes67player.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "onCreate called")
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Aes67PlayerApp()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i("MainActivity", "onStop called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("MainActivity", "onRestart called")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "onPause called")
    }

    public override fun onRestoreInstanceState(inState: Bundle) {
        Log.i("MainActivity", "onRestoreInstanceState called")
        super.onRestoreInstanceState(inState)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        Log.i("MainActivity", "onSaveInstanceState called")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "onDestroy called")
    }
}

