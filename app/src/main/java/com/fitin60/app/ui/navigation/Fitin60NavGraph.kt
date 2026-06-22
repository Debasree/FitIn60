package com.fitin60.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitin60.app.ui.screens.calendar.CalendarScreen
import com.fitin60.app.ui.screens.daydetail.DayDetailScreen
import com.fitin60.app.ui.screens.importplan.ImportPlanScreen
import com.fitin60.app.ui.screens.onboarding.OnboardingScreen
import com.fitin60.app.ui.screens.preview.PlanPreviewScreen
import com.fitin60.app.ui.screens.progress.ProgressScreen
import com.fitin60.app.ui.screens.settings.SettingsScreen
import com.fitin60.app.ui.screens.today.TodayScreen
import com.fitin60.app.ui.screens.weekly.WeeklyCheckinScreen
import com.fitin60.app.viewmodel.Fitin60ViewModel

@Composable
fun Fitin60NavGraph(viewModel: Fitin60ViewModel) {
    val navController = rememberNavController()
    val hasProgram by viewModel.hasProgram.collectAsStateWithLifecycle()

    val start = if (hasProgram == true) Destinations.Today else Destinations.Onboarding

    NavHost(navController = navController, startDestination = start) {
        composable(Destinations.Onboarding) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Destinations.Import) },
                onUseSample = {
                    viewModel.useSamplePlan {
                        navController.navigate(Destinations.Today) {
                            popUpTo(Destinations.Onboarding) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Destinations.Import) {
            ImportPlanScreen(
                viewModel = viewModel,
                onParsed = { navController.navigate(Destinations.Preview) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destinations.Preview) {
            PlanPreviewScreen(
                viewModel = viewModel,
                onConfirm = {
                    viewModel.confirmImport {
                        navController.navigate(Destinations.Today) {
                            popUpTo(Destinations.Onboarding) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destinations.Today) {
            TodayScreen(
                viewModel = viewModel,
                onOpenCalendar = { navController.navigate(Destinations.Calendar) },
                onOpenWeekly = { navController.navigate(Destinations.Weekly) },
                onOpenProgress = { navController.navigate(Destinations.Progress) },
                onOpenSettings = { navController.navigate(Destinations.Settings) },
                onOpenDay = { day -> navController.navigate(Destinations.dayDetail(day)) },
            )
        }
        composable(Destinations.Calendar) {
            CalendarScreen(
                viewModel = viewModel,
                onOpenDay = { day -> navController.navigate(Destinations.dayDetail(day)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Destinations.DayDetail,
            arguments = listOf(navArgument("day") { type = NavType.IntType }),
        ) { entry ->
            val day = entry.arguments?.getInt("day") ?: 1
            DayDetailScreen(
                viewModel = viewModel,
                dayNumber = day,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destinations.Weekly) {
            WeeklyCheckinScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destinations.Progress) {
            ProgressScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destinations.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onReset = {
                    viewModel.resetProgram {
                        navController.navigate(Destinations.Onboarding) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
    }
}
