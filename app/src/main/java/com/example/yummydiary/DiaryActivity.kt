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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yummydiary.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class DiaryActivity : ComponentActivity() {

    private val viewModel: MealViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        MealViewModel.Factory(MealRepository(database.mealDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadAllData()

        setContent {
            YummyDiaryTheme {
                DiaryScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    onMealClick = { mealId ->
                        val intent = Intent(this, MealDetailsActivity::class.java)
                        intent.putExtra("MEAL_ID", mealId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryScreen(
    viewModel: MealViewModel,
    onBackClick: () -> Unit,
    onMealClick: (Int) -> Unit
) {
    val meals by viewModel.meals.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    
    // Wymagana animacja: Płynne pojawianie się listy
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DZIENNIK DAŃ", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryYellow,
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
            Spacer(modifier = Modifier.height(8.dp))
            // Pole wyszukiwania (MVVM)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Szukaj posiłku lub restauracji...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryYellow,
                    unfocusedBorderColor = PrimaryYellow.copy(alpha = 0.5f),
                    focusedLabelColor = PrimaryYellow,
                    cursorColor = PrimaryYellow
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nowość: Filtrowanie po kategoriach
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = {
                            selectedCategories = if (category in selectedCategories) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryYellow,
                            selectedLabelColor = PrimaryDark,
                            containerColor = Color.Transparent,
                            labelColor = if (isSystemInDarkTheme()) Color.White else PrimaryDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = category in selectedCategories,
                            borderColor = PrimaryYellow.copy(alpha = 0.6f),
                            selectedBorderColor = PrimaryYellow,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically()
            ) {
                if (meals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Brak wpisów w dzienniku", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        val filteredMeals = meals.filter { meal ->
                            val matchesSearch = meal.mealName.contains(searchQuery, ignoreCase = true) ||
                                    meal.restaurantName.contains(searchQuery, ignoreCase = true)
                            
                            val mealCats = meal.category.split(",").map { it.trim() }
                            val matchesCategory = selectedCategories.isEmpty() || 
                                    selectedCategories.any { it in mealCats }
                            
                            matchesSearch && matchesCategory
                        }
                        
                        items(filteredMeals) { meal ->
                            MealCard(meal = meal, onClick = { onMealClick(meal.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCard(meal: Meal, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryDark,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Używamy Coil do ładowania obrazów w Compose (odpowiednik Glide)
            AsyncImage(
                model = meal.imagePath ?: R.drawable.ic_launcher_background,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = meal.mealName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = meal.restaurantName.ifEmpty { "Własny przepis" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
                
                val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(meal.date))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryYellow
                )
                
                Text(
                    text = "⭐ ".repeat(meal.rating.toInt()),
                    fontSize = 12.sp
                )
            }
        }
    }
}
