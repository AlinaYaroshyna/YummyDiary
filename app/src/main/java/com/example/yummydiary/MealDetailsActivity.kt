package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yummydiary.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MealDetailsActivity : ComponentActivity() {

    private val viewModel: MealViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        MealViewModel.Factory(MealRepository(database.mealDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mealId = intent.getIntExtra("MEAL_ID", -1)
        if (mealId != -1) {
            viewModel.loadMealById(mealId)
        } else {
            finish()
        }

        setContent {
            YummyDiaryTheme {
                val meal by viewModel.selectedMeal.collectAsState()
                
                meal?.let { currentMeal ->
                    MealDetailsScreen(
                        meal = currentMeal,
                        onBack = { finish() },
                        onEdit = {
                            val intent = Intent(this, AddMealActivity::class.java).apply {
                                putExtra("MEAL_ID", currentMeal.id)
                            }
                            startActivity(intent)
                        },
                        onDelete = {
                            viewModel.deleteMeal(currentMeal.id) {
                                finish()
                            }
                        },
                        onViewRecipe = {
                            if (currentMeal.recipeId != null) {
                                val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                                    putExtra("RECIPE_ID", currentMeal.recipeId)
                                }
                                startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailsScreen(
    meal: Meal,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewRecipe: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SZCZEGÓŁY DANIA", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = PrimaryDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = PrimaryDark)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = PrimaryDark)
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
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = meal.imagePath ?: R.drawable.ic_launcher_background,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = meal.mealName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (meal.restaurantName.isEmpty()) "Własny przepis" else meal.restaurantName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (meal.restaurantAddress.isNotEmpty()) {
                    Text(text = meal.restaurantAddress, style = MaterialTheme.typography.bodyMedium)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                DetailRow(label = "Kategoria", value = meal.category)
                DetailRow(label = "Ocena", value = "⭐ ".repeat(meal.rating.toInt()))
                
                val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("pl"))
                DetailRow(label = "Data", value = sdf.format(Date(meal.date)))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Opis:", fontWeight = FontWeight.Bold)
                Text(text = meal.description, style = MaterialTheme.typography.bodyLarge)

                if (meal.recipeId != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onViewRecipe,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryYellow,
                            contentColor = PrimaryDark
                        )
                    ) {
                        Text("Zobacz przepis")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}
