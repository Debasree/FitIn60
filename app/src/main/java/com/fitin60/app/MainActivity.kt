package com.fitin60.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fitin60.app.ui.navigation.Fitin60NavGraph
import com.fitin60.app.ui.theme.Fitin60Theme
import com.fitin60.app.ui.theme.SurfaceBotDark
import com.fitin60.app.ui.theme.SurfaceBotLight
import com.fitin60.app.ui.theme.SurfaceTopDark
import com.fitin60.app.ui.theme.SurfaceTopLight
import com.fitin60.app.viewmodel.Fitin60ViewModel
import com.fitin60.app.viewmodel.Fitin60ViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<Fitin60ViewModel> {
        Fitin60ViewModelFactory((application as Fitin60App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fitin60Theme {
                AppBackdrop {
                    Fitin60NavGraph(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun AppBackdrop(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (darkTheme)
                            listOf(SurfaceTopDark, SurfaceBotDark)
                        else
                            listOf(SurfaceTopLight, SurfaceBotLight),
                    )
                )
        ) {
            content()
        }
    }
}
