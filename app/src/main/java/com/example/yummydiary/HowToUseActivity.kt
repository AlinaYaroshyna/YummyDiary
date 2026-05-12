package com.example.yummydiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummydiary.ui.theme.PrimaryDark
import com.example.yummydiary.ui.theme.YummyDiaryTheme

class HowToUseActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            YummyDiaryTheme {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("JAK UŻYWAĆ", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Wstecz",
                                        tint = PrimaryDark
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFFE0E0E0),
                                titleContentColor = PrimaryDark
                            )
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            Text(
                                text = "Obejrzyj poniższy film, aby dowiedzieć się, jak w pełni korzystać z możliwości Twojego kulinarno-podróżniczego dziennika:",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }

                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Pozwala filmikowi zająć dostępną przestrzeń
                                .padding(vertical = 8.dp)
                        ) {
                            VideoPlayer(
                                videoResId = R.raw.how_to_use,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

