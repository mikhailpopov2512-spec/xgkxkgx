package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.GameViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: GameViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val businesses by viewModel.businesses.collectAsState()
    val transactionSuccess by viewModel.showTransactionSuccess.collectAsState()

    // Hook (LaunchedEffect) ticking once a second, calculating overall hourly income of all owned businesses and adding the proportional second-slice to the player state/balance:
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(1000L)
            viewModel.accrueBusinessPassiveIncomeOneSecond()
        }
    }

    // Dynamically calculate warnings warning count for Bottom bar badge!
    val alertCount = businesses.count { it.isWarningActive && it.level > 0 }

    // Centralized elegant dark navigation theme colors
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF1D192B), // Deep dark selection focus
        selectedTextColor = GameWhite,
        indicatorColor = GameGreenAccent, // Soft lavender background capsule
        unselectedIconColor = GameGrayText,
        unselectedTextColor = GameGrayText
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = GameBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = GameSurfaceDark,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Tab 0: БИЗНЕС
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.setCurrentTab(0) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (alertCount > 0) {
                                        Badge(
                                            containerColor = GameGoldBadge,
                                            contentColor = Color.Black
                                        ) {
                                            Text("$alertCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BusinessCenter,
                                    contentDescription = "Бизнес"
                                )
                            }
                        },
                        label = { Text("Бизнес", fontSize = 11.sp) },
                        colors = navItemColors
                    )

                    // Tab 1: ИНВЕСТИЦИИ
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.setCurrentTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Инвестиции"
                            )
                        },
                        label = { Text("Инвестиции", fontSize = 11.sp) },
                        colors = navItemColors
                    )

                    // Tab 2: ЗАРАБОТОК (The Clicker default)
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.setCurrentTab(2) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Заработок"
                            )
                        },
                        label = { Text("Заработок", fontSize = 11.sp) },
                        colors = navItemColors
                    )

                    // Tab 3: ЭКСКЛЮЗИВ (Leaderboard)
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.setCurrentTab(3) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Эксклюзив"
                            )
                        },
                        label = { Text("Эксклюзив", fontSize = 11.sp) },
                        colors = navItemColors
                    )

                    // Tab 4: АККАУНТ
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { viewModel.setCurrentTab(4) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Аккаунт"
                            )
                        },
                        label = { Text("Аккаунт", fontSize = 11.sp) },
                        colors = navItemColors
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> BusinessScreen(viewModel = viewModel)
                    1 -> InvestmentsScreen(viewModel = viewModel)
                    2 -> EarningsScreen(viewModel = viewModel)
                    3 -> LeaderboardScreen(viewModel = viewModel)
                    4 -> AccountScreen(viewModel = viewModel)
                    else -> EarningsScreen(viewModel = viewModel)
                }
            }
        }

        // Animated Success HUD overlay at top of screen
        AnimatedVisibility(
            visible = transactionSuccess != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp)
                .zIndex(99f)
        ) {
            transactionSuccess?.let { message ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = GameSurface,
                        contentColor = GameWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, GameAccentPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(GameGreenAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Успешно",
                                tint = Color(0xFF1D192B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = message,
                            color = GameWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
