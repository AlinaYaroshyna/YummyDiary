package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yummydiary.ui.theme.*

class AllRecipesActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        RecipeViewModel.Factory(RecipeRepository(database.recipeDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadRecipes()

        setContent {
            YummyDiaryTheme {
                val recipes by viewModel.recipes.collectAsState()
                val categories by viewModel.categories.collectAsState()
                
                var searchQuery by remember { mutableStateOf("") }
                var selectedCategories by remember { mutableStateOf(setOf<String>()) }

                val filteredRecipes = recipes.filter { item ->
                    val matchesSearch = searchQuery.isEmpty() || 
                        (item.meal?.mealName?.contains(searchQuery, ignoreCase = true) ?: false) ||
                        item.recipe.ingredients.contains(searchQuery, ignoreCase = true)
                    
                    val itemCats = item.meal?.category?.split(",")?.map { it.trim() } ?: emptyList()
                    val matchesCategory = selectedCategories.isEmpty() || 
                        selectedCategories.any { it in itemCats }
                    
                    matchesSearch && matchesCategory
                }

                RecipesScreen(
                    recipes = filteredRecipes,
                    categories = categories,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategories = selectedCategories,
                    onCategoryToggle = { category ->
                        selectedCategories = if (category in selectedCategories) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    },
                    onRecipeClick = { recipeWithMeal ->
                        val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                            putExtra("RECIPE_ID", recipeWithMeal.recipe.id)
                        }
                        startActivity(intent)
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipesScreen(
    recipes: List<RecipeWithMeal>,
    categories: List<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onRecipeClick: (RecipeWithMeal) -> Unit,
    onBack: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WSZYSTKIE PRZEPISY", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = PrimaryDark
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Szukaj potrawy lub składnika...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f),
                    focusedLabelColor = PrimaryGreen,
                    cursorColor = PrimaryGreen
                )
            )

            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = { onCategoryToggle(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = PrimaryDark,
                            containerColor = Color.Transparent,
                            labelColor = if (isSystemInDarkTheme()) Color.White else PrimaryDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = category in selectedCategories,
                            borderColor = PrimaryGreen.copy(alpha = 0.6f),
                            selectedBorderColor = PrimaryGreen,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically()
            ) {
                if (recipes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Brak przepisów", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recipes) { item ->
                            RecipeCard(item, onClick = { onRecipeClick(item) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCard(item: RecipeWithMeal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryDark,
            contentColor = Color.White
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AsyncImage(
                model = item.meal?.imagePath ?: R.drawable.ic_launcher_background,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = item.meal?.mealName ?: "Bez nazwy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.meal?.category ?: "Brak kategorii",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                Text(
                    text = "Składniki: ${item.recipe.ingredients.take(50)}...",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = PrimaryGreen
                )
            }
        }
    }
}
