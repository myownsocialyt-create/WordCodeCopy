package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ads.AdManager
import com.example.data.Category
import com.example.data.CategoryRepository
import com.example.engine.SoundManager
import com.example.model.GameMode
import com.example.ui.screens.CategorySelectScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModeSelectScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

private enum class Screen {
    HOME, CATEGORY_SELECT, MODE_SELECT, GAME, RESULTS
}

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AdManager.initialize(this)

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WordSearchApp(
                        viewModel = gameViewModel,
                        modifier = Modifier.padding(innerPadding),
                        onExitApp = {
                            AdManager.showAppClosingInterstitialIfEligible(this) {
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            SoundManager.release()
        }
    }
}

@Composable
private fun WordSearchApp(
    viewModel: GameViewModel,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val coins by viewModel.userCoins.collectAsState()
    val completedLevels by viewModel.completedLevels.collectAsState()
    val unlockedLevels by viewModel.unlockedLevels.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val selectedCategory: Category? = selectedCategoryId?.let { CategoryRepository.getCategoryById(it) }

    when (currentScreen) {
        Screen.HOME -> {
            androidx.activity.compose.BackHandler { onExitApp() }
            HomeScreen(
                coins = coins,
                completedLevels = completedLevels,
                unlockedLevels = unlockedLevels,
                onPlayClick = { currentScreen = Screen.CATEGORY_SELECT },
                modifier = modifier
            )
        }

        Screen.CATEGORY_SELECT -> {
            androidx.activity.compose.BackHandler { currentScreen = Screen.HOME }
            CategorySelectScreen(
                coins = coins,
                completedLevels = completedLevels,
                unlockedLevels = unlockedLevels,
                onCategorySelected = { category ->
                    selectedCategoryId = category.id
                    currentScreen = Screen.MODE_SELECT
                },
                onUnlockWithCoins = { category ->
                    viewModel.unlockCategoryWithCoins(category.id)
                },
                onWatchAdToEarnCoins = {
                    // Handled inside screens via rewarded ad dialogs
                },
                onBack = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }

        Screen.MODE_SELECT -> {
            val category = selectedCategory
            if (category == null) {
                currentScreen = Screen.CATEGORY_SELECT
            } else {
                androidx.activity.compose.BackHandler { currentScreen = Screen.CATEGORY_SELECT }
                ModeSelectScreen(
                    category = category,
                    coins = coins,
                    onStartGame = { cat, mode ->
                        viewModel.startNewGame(cat.id, mode)
                        currentScreen = Screen.GAME
                    },
                    onBack = { currentScreen = Screen.CATEGORY_SELECT },
                    modifier = modifier
                )
            }
        }

        Screen.GAME -> {
            GameScreen(
                viewModel = viewModel,
                onNavigateToResults = { currentScreen = Screen.RESULTS },
                onBack = { currentScreen = Screen.CATEGORY_SELECT },
                modifier = modifier
            )
        }

        Screen.RESULTS -> {
            androidx.activity.compose.BackHandler { currentScreen = Screen.HOME }
            ResultsScreen(
                uiState = uiState,
                onPlayAgain = {
                    val catId = uiState.category?.id ?: selectedCategoryId
                    if (catId != null) {
                        viewModel.startNewGame(catId, uiState.mode)
                        currentScreen = Screen.GAME
                    } else {
                        currentScreen = Screen.HOME
                    }
                },
                onNextLevel = { nextCategory ->
                    selectedCategoryId = nextCategory.id
                    currentScreen = Screen.MODE_SELECT
                },
                onHome = { currentScreen = Screen.HOME },
                modifier = modifier
            )
        }
    }
}
