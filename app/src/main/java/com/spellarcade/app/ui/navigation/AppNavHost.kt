package com.spellarcade.app.ui.navigation

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.spellarcade.app.util.MusicManager
import com.spellarcade.app.ui.screens.games.*
import com.spellarcade.app.ui.screens.home.HomeScreen
import com.spellarcade.app.ui.screens.lists.ListsScreen
import com.spellarcade.app.ui.screens.parent.ParentScreen
import com.spellarcade.app.ui.screens.profile.AvatarBuilderScreen
import com.spellarcade.app.ui.screens.progress.ProgressScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Games : Screen("games", "Games", Icons.Default.SportsEsports)
    data object Progress : Screen("progress", "Progress", Icons.Default.Star)
    data object Lists : Screen("lists", "Lessons", Icons.Default.School)
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Games, Screen.Progress, Screen.Lists)
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainAreaRoutes = remember {
        setOf(
            Screen.Home.route,
            Screen.Games.route,
            Screen.Progress.route,
            Screen.Lists.route,
            "parent"
        )
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "avatar_builder") {
            MusicManager.playMusic(context, "music/hero_build.wav")
        } else if (currentRoute in mainAreaRoutes) {
            MusicManager.playMusic(context, "music/main_theme.ogg")
        } else if (currentRoute == "game/hangman") {
            MusicManager.playMusic(context, "music/lab_meltdown.ogg")
        } else if (currentRoute?.startsWith("game/") == true) {
            MusicManager.pauseMusic()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> MusicManager.pauseMusic()
                Lifecycle.Event.ON_RESUME -> {
                    if (currentRoute == "avatar_builder") {
                        MusicManager.playMusic(context, "music/hero_build.wav")
                    } else if (currentRoute in mainAreaRoutes) {
                        MusicManager.playMusic(context, "music/main_theme.ogg")
                    } else if (currentRoute == "game/hangman") {
                        MusicManager.playMusic(context, "music/lab_meltdown.ogg")
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    val activity = context as? Activity
                    if (activity == null || activity.isFinishing) {
                        MusicManager.stopMusic()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        bottomBar = {
            // Hide bottom bar on game screens and parent
            val showBottomBar = currentRoute in items.map { it.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF010A1A), // Even darker blue
                    tonalElevation = 0.dp
                ) {
                    items.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { 
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 56.dp else 40.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(0xFF1E88E5).copy(alpha = 0.2f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        screen.icon, 
                                        contentDescription = screen.title,
                                        tint = if (isSelected) Color(0xFF00B0FF) else Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(if (isSelected) 30.dp else 24.dp)
                                    )
                                }
                            },
                            label = { 
                                Text(
                                    screen.title, 
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ) 
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToGames = { navController.navigate(Screen.Games.route) },
                    onNavigateToLists = { navController.navigate(Screen.Lists.route) },
                    onNavigateToParent = { navController.navigate("parent") },
                    onNavigateToProfile = { navController.navigate("avatar_builder") }
                )
            }
            composable(Screen.Games.route) {
                GamesMenuScreen(
                    onGameSelected = { gameId ->
                        navController.navigate("game/$gameId")
                    }
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Lists.route) {
                ListsScreen(
                    onOpenParent = { navController.navigate("parent") }
                )
            }
            composable("parent") {
                ParentScreen(onBack = { navController.popBackStack() })
            }
            composable("avatar_builder") {
                AvatarBuilderScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "game/{gameId}",
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
                when (gameId) {
                    "hangman" -> LabMeltdownScreen(onBack = { navController.popBackStack() })
                    "pirate_blaster" -> PirateBlasterScreen(onBack = { navController.popBackStack() })
                    "missing_letters" -> MissingLettersScreen(onBack = { navController.popBackStack() })
                    "word_gallery" -> {
                        val context = LocalContext.current
                        WordGalleryInfoScreen(
                            onBack = {
                                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                navController.popBackStack()
                            },
                            onLaunchAttraction = { attractionId ->
                                if (attractionId == "pirate_blaster") {
                                    navController.navigate("pirate_blaster")
                                }
                            }
                        )
                    }
                    "scrambled" -> ScrambledWordScreen(onBack = { navController.popBackStack() })
                    "listen_spell" -> ListenSpellScreen(onBack = { navController.popBackStack() })
                    "which_correct" -> WhichCorrectScreen(onBack = { navController.popBackStack() })
                    "challenge" -> ChallengeScreen(onBack = { navController.popBackStack() })
                    else -> {
                        navController.popBackStack()
                    }
                }
            }
            composable("pirate_blaster") {
                PirateBlasterScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
