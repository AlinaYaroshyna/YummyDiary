package com.example.yummydiary

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.yummydiary.ui.theme.*

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YummyDiaryTheme {
                MainMenuScreen(
                    onNavigate = { activityClass ->
                        startActivity(Intent(this, activityClass))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(onNavigate: (Class<*>) -> Unit) {
    val context = LocalContext.current
    
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("YUMMY DIARY", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val screenHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .heightIn(min = screenHeight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // Logo
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.logo_animated, imageLoader),
                        contentDescription = "Logo",
                        modifier = Modifier.size(130.dp).padding(vertical = 8.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // 1. MAPA DAŃ - Szeroki przycisk na górze
                    WideMenuCard(
                        item = MenuItemData("Mapa dań", Icons.Default.Place, MealMapActivity::class.java, PrimaryDark),
                        onNavigate = onNavigate
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Siatka 4 głównych przycisków
                    val menuItems = listOf(
                        MenuItemData("Dodaj danie", Icons.Default.Add, AddMealActivity::class.java, PrimaryRed),
                        MenuItemData("Dziennik dań", Icons.AutoMirrored.Filled.List, DiaryActivity::class.java, PrimaryYellow),
                        MenuItemData("Dodaj przepis", Icons.Default.Add, AddRecipeActivity::class.java, PrimaryBlue),
                        MenuItemData("Wszystkie przepisy", Icons.AutoMirrored.Filled.List, AllRecipesActivity::class.java, PrimaryGreen)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            MenuCard(menuItems[0], onNavigate, Modifier.weight(1f))
                            MenuCard(menuItems[1], onNavigate, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            MenuCard(menuItems[2], onNavigate, Modifier.weight(1f))
                            MenuCard(menuItems[3], onNavigate, Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Miejsce na dolne przyciski
                }
            }

            // DOLNY PASEK: Jak używać (lewo) i Ustawienia (prawo)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onNavigate(HowToUseActivity::class.java) }) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Jak używać", modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = { onNavigate(SettingsActivity::class.java) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ustawienia", modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WideMenuCard(item: MenuItemData, onNavigate: (Class<*>) -> Unit) {
    Surface(
        onClick = { onNavigate(item.activityClass) },
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = item.backgroundColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(item.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(item: MenuItemData, onNavigate: (Class<*>) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = { onNavigate(item.activityClass) },
            modifier = Modifier.size(105.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = item.backgroundColor,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = PrimaryDark
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

data class MenuItemData(
    val label: String,
    val icon: ImageVector,
    val activityClass: Class<*>,
    val backgroundColor: Color
)
