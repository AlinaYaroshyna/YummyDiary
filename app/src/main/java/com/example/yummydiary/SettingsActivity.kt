package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummydiary.ui.theme.PrimaryDark
import com.example.yummydiary.ui.theme.YummyDiaryTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YummyDiaryTheme {
                SettingsScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("USTAWIENIA", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                SettingsCategoryHeader("Zarządzanie treścią")
            }
            item {
                SettingsItem(
                    title = "Edytuj kategorie",
                    summary = "Dodawaj, usuwaj i zmieniaj nazwy kategorii dań",
                    icon = Icons.Default.Category,
                    onClick = {
                        context.startActivity(Intent(context, EditCategoriesActivity::class.java))
                    }
                )
            }
            item {
                SettingsItem(
                    title = "Zarządzanie zdjęciami",
                    summary = "Oczyść nieużywane zdjęcia i zarządzaj pamięcią",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = {
                        Toast.makeText(context, "Zarządzanie zdjęciami wkrótce!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            
            item {
                SettingsCategoryHeader("Dane i kopia zapasowa")
            }
            item {
                SettingsItem(
                    title = "Eksportuj dane",
                    summary = "Zapisz swoje dania i przepisy do pliku",
                    icon = Icons.Default.CloudUpload,
                    onClick = {
                        Toast.makeText(context, "Eksport danych wkrótce!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                SettingsItem(
                    title = "Importuj dane",
                    summary = "Wczytaj dane z kopii zapasowej",
                    icon = Icons.Default.CloudDownload,
                    onClick = {
                        Toast.makeText(context, "Import danych wkrótce!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    summary: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
