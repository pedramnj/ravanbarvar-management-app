package com.ravanbarvar.patientmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ravanbarvar.patientmanager.ui.calendar.CalendarScreen
import com.ravanbarvar.patientmanager.ui.dashboard.DashboardScreen
import com.ravanbarvar.patientmanager.ui.login.LoginScreen
import com.ravanbarvar.patientmanager.ui.patients.PatientDetailScreen
import com.ravanbarvar.patientmanager.ui.patients.PatientListScreen
import com.ravanbarvar.patientmanager.ui.settings.SettingsScreen
import com.ravanbarvar.patientmanager.ui.splash.SplashScreen

@Composable
fun RavanbarvarNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(onResolved = { loggedIn ->
                navController.navigate(if (loggedIn) Routes.Dashboard else Routes.Login) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            })
        }

        composable(Routes.Login) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.Dashboard) {
                    popUpTo(Routes.Login) { inclusive = true }
                }
            })
        }

        composable(Routes.Dashboard) {
            DashboardScreen(
                onAddPatient = { navController.navigate(Routes.patientDetail(-1L)) },
                onOpenCalendar = {
                    navController.navigate(Routes.Calendar) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Routes.Calendar) { CalendarScreen() }

        composable(Routes.Patients) {
            PatientListScreen(
                onPatientClick = { id -> navController.navigate(Routes.patientDetail(id)) },
                onAddPatient = { navController.navigate(Routes.patientDetail(-1L)) }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(onLoggedOut = {
                navController.navigate(Routes.Login) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            })
        }

        composable(
            route = Routes.PatientDetailPattern,
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L
            PatientDetailScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }
    }
}
