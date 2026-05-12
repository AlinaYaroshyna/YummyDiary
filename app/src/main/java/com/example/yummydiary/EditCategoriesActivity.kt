package com.example.yummydiary

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummydiary.ui.theme.PrimaryDark
import com.example.yummydiary.ui.theme.YummyDiaryTheme

class EditCategoriesActivity : ComponentActivity() {

    private val viewModel: MealViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        MealViewModel.Factory(MealRepository(database.mealDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadAllData()

        setContent {
            YummyDiaryTheme {
                EditCategoriesScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoriesScreen(
    viewModel: MealViewModel,
    onBackClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    var categoryToEdit by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EDYTUJ KATEGORIE", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
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
        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Brak kategorii do edycji", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        name = category,
                        onEdit = { categoryToEdit = category },
                        onDelete = { categoryToDelete = category }
                    )
                }
            }
        }

        // Dialog Edycji
        categoryToEdit?.let { oldName ->
            var newName by remember { mutableStateOf(oldName) }
            AlertDialog(
                onDismissRequest = { categoryToEdit = null },
                title = { Text("Zmień nazwę kategorii") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nazwa kategorii") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank() && newName != oldName) {
                            viewModel.updateCategoryName(oldName, newName)
                            Toast.makeText(context, "Zaktualizowano!", Toast.LENGTH_SHORT).show()
                        }
                        categoryToEdit = null
                    }) {
                        Text("Zapisz")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToEdit = null }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        // Dialog Usuwania
        categoryToDelete?.let { category ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Usuń kategorię") },
                text = { Text("Czy na pewno chcesz usunąć kategorię '$category' ze wszystkich dań?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCategory(category)
                        Toast.makeText(context, "Usunięto!", Toast.LENGTH_SHORT).show()
                        categoryToDelete = null
                    }) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryItem(name: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
