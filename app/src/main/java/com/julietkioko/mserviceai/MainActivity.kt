package com.julietkioko.mserviceai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.julietkioko.mserviceai.core.navigation.MServiceApp
import com.julietkioko.mserviceai.core.ui.theme.MServiceTheme

/**
 * Single-activity entry point for MService AI.
 *
 * This follows the **single-activity architecture** recommended by Google:
 * one Activity hosts all screens via Compose Navigation. The Activity
 * does only three things:
 * 1. [enableEdgeToEdge] — draws behind the system bars for a modern look.
 * 2. Wraps everything in [MServiceTheme] — applies Material 3 styling.
 * 3. Delegates to [MServiceApp] — the root composable that owns the
 *    [NavHost] and bottom navigation bar.
 *
 * By keeping the Activity thin, all UI logic lives in composables which
 * are easier to test, preview, and reuse.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MServiceTheme {
                MServiceApp()
            }
        }
    }
}
