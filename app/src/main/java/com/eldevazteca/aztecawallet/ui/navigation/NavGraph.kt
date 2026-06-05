package com.eldevazteca.aztecawallet.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eldevazteca.aztecawallet.ui.screens.accounts.AccountsScreen
import com.eldevazteca.aztecawallet.ui.screens.categories.CategoriesScreen
import com.eldevazteca.aztecawallet.ui.screens.dashboard.DashboardScreen
import com.eldevazteca.aztecawallet.ui.screens.reports.ReportsScreen
import com.eldevazteca.aztecawallet.ui.screens.settings.SettingsScreen
import com.eldevazteca.aztecawallet.ui.screens.transactions.TransactionFormScreen
import com.eldevazteca.aztecawallet.ui.screens.transactions.TransactionsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    data object Transactions : Screen("transactions", "Movimientos", Icons.Default.Receipt)
    data object TransactionForm : Screen("transaction_form?transactionId={transactionId}", "Transacción", Icons.Default.Receipt) {
        fun createRoute(transactionId: Long? = null): String {
            return if (transactionId != null) "transaction_form?transactionId=$transactionId"
            else "transaction_form"
        }
    }
    data object Categories : Screen("categories", "Categorías", Icons.Default.Category)
    data object Accounts : Screen("accounts", "Cuentas", Icons.Default.AccountBalance)
    data object Reports : Screen("reports", "Reportes", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Config", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Reports,
    Screen.Settings
)

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AztecaBottomNav(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    onBack = { navController.popBackStack() },
                    onAddTransaction = { navController.navigate(Screen.TransactionForm.createRoute()) },
                    onEditTransaction = { id -> navController.navigate(Screen.TransactionForm.createRoute(id)) }
                )
            }

            composable(
                route = Screen.TransactionForm.route,
                arguments = listOf(navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId")?.takeIf { it != -1L }
                TransactionFormScreen(
                    transactionId = transactionId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Accounts.route) {
                AccountsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Reports.route) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun AztecaBottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isDark = isSystemInDarkTheme()

    val bgColor = if (isDark) MaterialTheme.colorScheme.surfaceContainer
    else MaterialTheme.colorScheme.surface

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(
        alpha = if (isDark) 0.3f else 1f
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shadowElevation = if (isDark) 8.dp else 2.dp,
        shape = RoundedCornerShape(
            topStart = if (isDark) 24.dp else 12.dp,
            topEnd = if (isDark) 24.dp else 12.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                BottomNavItem(
                    screen = screen,
                    selected = selected,
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

@Composable
private fun BottomNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    val bgModifier = if (selected) {
        Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    } else {
        Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    }

    Column(
        modifier = bgModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = screen.title,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.05.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

