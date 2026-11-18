package com.sherakbar.family

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sherakbar.family.ui.navigation.Screen
import com.sherakbar.family.ui.navigation.bottomNavItems
import com.sherakbar.family.ui.screens.AboutFamilyScreen
import com.sherakbar.family.ui.screens.AdminDashboardScreen
import com.sherakbar.family.ui.screens.AnnouncementsScreen
import com.sherakbar.family.ui.screens.ContactAdminScreen
import com.sherakbar.family.ui.screens.FamilyTreeScreen
import com.sherakbar.family.ui.screens.GalleryScreen
import com.sherakbar.family.ui.screens.HomeScreen
import com.sherakbar.family.ui.screens.LoginScreen
import com.sherakbar.family.ui.screens.MemberDetailScreen
import com.sherakbar.family.ui.screens.MemberFormScreen
import com.sherakbar.family.ui.screens.MembersDirectoryScreen
import com.sherakbar.family.ui.screens.MoreMenuScreen
import com.sherakbar.family.ui.screens.RegisterScreen
import com.sherakbar.family.ui.screens.SettingsScreen
import com.sherakbar.family.ui.screens.SplashScreenRoute
import com.sherakbar.family.ui.theme.ForestGreen
import com.sherakbar.family.ui.theme.RoyalGold
import com.sherakbar.family.ui.theme.SherAkbarTheme
import com.sherakbar.family.ui.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val darkMode by viewModel.darkMode.collectAsState()
            val languageCode by viewModel.languageCode.collectAsState()

            val baseContext = LocalContext.current
            val localizedContext = remember(languageCode, baseContext) {
                createLocalizedContext(baseContext, languageCode)
            }
            val layoutDirection = if (languageCode == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalLayoutDirection provides layoutDirection
            ) {
                SherAkbarTheme(darkTheme = darkMode) {
                    SherAkbarAppShell(viewModel = viewModel)
                }
            }
        }
    }

    private fun createLocalizedContext(base: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}

@Composable
fun SherAkbarAppShell(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by viewModel.currentUser.collectAsState()
    val authInitialized by viewModel.authInitialized.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val topLevelRoutes = remember { bottomNavItems.map { it.route }.toSet() }
    val showBottomBar = currentRoute in topLevelRoutes && currentUser != null

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = ForestGreen
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = {
                                Text(
                                    text = stringResource(item.labelRes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ForestGreen,
                                selectedTextColor = ForestGreen,
                                indicatorColor = RoyalGold.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreenRoute(
                        authInitialized = authInitialized,
                        isLoggedIn = currentUser != null,
                        onFinished = { loggedIn ->
                            val target = if (loggedIn) Screen.Home.route else Screen.Login.route
                            navController.navigate(target) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = viewModel,
                        onBackToLogin = { navController.popBackStack() }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateTree = { navController.navigate(Screen.FamilyTree.route) },
                        onNavigateMembers = { navController.navigate(Screen.Members.route) },
                        onNavigateGallery = { navController.navigate(Screen.Gallery.route) },
                        onNavigateAnnouncements = { navController.navigate(Screen.Announcements.route) },
                        onNavigateAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                        onOpenMember = { id -> navController.navigate(Screen.MemberDetail.createRoute(id)) }
                    )
                }

                composable(Screen.FamilyTree.route) {
                    FamilyTreeScreen(
                        viewModel = viewModel,
                        onOpenMember = { id -> navController.navigate(Screen.MemberDetail.createRoute(id)) },
                        onAddMember = { navController.navigate(Screen.MemberForm.createRoute()) },
                        onEditMember = { id -> navController.navigate(Screen.MemberForm.createRoute(id)) }
                    )
                }

                composable(Screen.Members.route) {
                    MembersDirectoryScreen(
                        viewModel = viewModel,
                        onOpenMember = { id -> navController.navigate(Screen.MemberDetail.createRoute(id)) },
                        onAddMember = { navController.navigate(Screen.MemberForm.createRoute()) }
                    )
                }

                composable(Screen.Gallery.route) {
                    GalleryScreen(viewModel = viewModel)
                }

                composable(Screen.More.route) {
                    MoreMenuScreen(
                        viewModel = viewModel,
                        onNavigateAnnouncements = { navController.navigate(Screen.Announcements.route) },
                        onNavigateAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                        onNavigateAbout = { navController.navigate(Screen.AboutFamily.route) },
                        onNavigateContact = { navController.navigate(Screen.ContactAdmin.route) },
                        onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                        onLogout = {
                            viewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = Screen.MemberDetail.route,
                    arguments = listOf(navArgument("memberId") { type = NavType.StringType })
                ) { entry ->
                    val memberId = entry.arguments?.getString("memberId").orEmpty()
                    MemberDetailScreen(
                        memberId = memberId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenMember = { id -> navController.navigate(Screen.MemberDetail.createRoute(id)) },
                        onEditMember = { id -> navController.navigate(Screen.MemberForm.createRoute(id)) }
                    )
                }

                composable(
                    route = Screen.MemberForm.route,
                    arguments = listOf(navArgument("memberId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { entry ->
                    val memberId = entry.arguments?.getString("memberId")
                    MemberFormScreen(
                        memberId = memberId,
                        viewModel = viewModel,
                        onDone = { navController.popBackStack() }
                    )
                }

                composable(Screen.Announcements.route) {
                    AnnouncementsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onAddMember = { navController.navigate(Screen.MemberForm.createRoute()) },
                        onNavigateAnnouncements = { navController.navigate(Screen.Announcements.route) },
                        onNavigateGallery = { navController.navigate(Screen.Gallery.route) },
                        onNavigateTree = { navController.navigate(Screen.FamilyTree.route) }
                    )
                }

                composable(Screen.AboutFamily.route) {
                    AboutFamilyScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.ContactAdmin.route) {
                    ContactAdminScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
