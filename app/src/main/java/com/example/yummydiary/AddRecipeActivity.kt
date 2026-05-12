package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yummydiary.ui.theme.*

class AddRecipeActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        RecipeViewModel.Factory(RecipeRepository(database.recipeDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val editingRecipeId = intent.getIntExtra("RECIPE_ID", -1).takeIf { it != -1 }
        if (editingRecipeId != null) {
            viewModel.loadRecipeById(editingRecipeId)
        }

        setContent {
            YummyDiaryTheme {
                val recipeToEdit by viewModel.selectedRecipe.collectAsState()
                
                var ingredients by remember { mutableStateOf(listOf("")) }
                var instructions by remember { mutableStateOf("") }
                var customMealName by remember { mutableStateOf("") }
                
                val mealName = intent.getStringExtra("MEAL_NAME") ?: ""
                val restaurantName = intent.getStringExtra("RESTAURANT_NAME") ?: ""
                val isStandalone = mealName.isEmpty() && editingRecipeId == null

                LaunchedEffect(recipeToEdit) {
                    recipeToEdit?.let {
                        ingredients = it.ingredients.split("\n")
                        instructions = it.instructions ?: ""
                    }
                }

                AddRecipeScreen(
                    mealName = mealName,
                    restaurantName = restaurantName,
                    isStandalone = isStandalone,
                    customMealName = customMealName,
                    onCustomMealNameChange = { customMealName = it },
                    ingredients = ingredients,
                    onIngredientsChange = { ingredients = it },
                    instructions = instructions,
                    onInstructionsChange = { instructions = it },
                    onSave = {
                        val finalIngredients = ingredients.filter { it.isNotBlank() }.joinToString("\n")
                        if (finalIngredients.isEmpty()) {
                            Toast.makeText(this@AddRecipeActivity, "Dodaj składniki", Toast.LENGTH_SHORT).show()
                        } else {
                            val recipe = Recipe(
                                id = editingRecipeId ?: 0,
                                ingredients = finalIngredients,
                                instructions = instructions.takeIf { it.isNotBlank() }
                            )
                            viewModel.saveRecipe(recipe) { newId ->
                                val resultIntent = Intent().apply {
                                    putExtra("RECIPE_ID", newId.toInt())
                                }
                                setResult(RESULT_OK, resultIntent)
                                Toast.makeText(this@AddRecipeActivity, "Przepis zapisany!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    mealName: String,
    restaurantName: String,
    isStandalone: Boolean,
    customMealName: String,
    onCustomMealNameChange: (String) -> Unit,
    ingredients: List<String>,
    onIngredientsChange: (List<String>) -> Unit,
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DODAJ PRZEPIS", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
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
                    containerColor = PrimaryBlue,
                    titleContentColor = PrimaryDark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isStandalone) {
                OutlinedTextField(
                    value = customMealName,
                    onValueChange = onCustomMealNameChange,
                    label = { Text("Nazwa dania") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = mealName, style = MaterialTheme.typography.headlineSmall)
                Text(text = if (restaurantName.isEmpty()) "Własne danie" else restaurantName, style = MaterialTheme.typography.bodyMedium)
            }

            Text(text = "Składniki:", style = MaterialTheme.typography.titleMedium)
            ingredients.forEachIndexed { index, ingredient ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredient,
                        onValueChange = { newValue ->
                            val newList = ingredients.toMutableList()
                            newList[index] = newValue
                            onIngredientsChange(newList)
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Składnik ${index + 1}") }
                    )
                    IconButton(onClick = {
                        val newList = ingredients.toMutableList()
                        newList.removeAt(index)
                        onIngredientsChange(newList)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń")
                    }
                }
            }
            
            Button(
                onClick = { onIngredientsChange(ingredients + "") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = PrimaryDark
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Dodaj składnik")
            }

            OutlinedTextField(
                value = instructions,
                onValueChange = onInstructionsChange,
                label = { Text("Instrukcje (opcjonalnie)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = PrimaryDark
                )
            ) {
                Text("Zapisz przepis")
            }
        }
    }
}
