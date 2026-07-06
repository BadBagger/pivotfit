package com.pivotfit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pivotfit.app.ui.PivotFitRoot
import com.pivotfit.app.ui.theme.PivotFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as PivotFitApp).repository
        setContent {
            PivotFitTheme {
                PivotFitRoot(repository)
            }
        }
    }
}
