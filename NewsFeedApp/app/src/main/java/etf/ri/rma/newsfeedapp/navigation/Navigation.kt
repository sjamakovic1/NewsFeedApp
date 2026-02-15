package etf.ri.rma.newsfeedapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import etf.ri.rma.newsfeedapp.screen.NewsDetailsScreen

@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "/home"
    ){
        composable("/home"){
            NewsFeedScreen(navController)
        }
        composable("/filters"){
            FilterScreen(navController)
        }
        composable(
            route = "/details/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            NewsDetailsScreen(id = id, navController = navController)
        }
    }
}