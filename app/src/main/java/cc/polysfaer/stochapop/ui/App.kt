package cc.polysfaer.stochapop.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.ui.navigation.AppNavHost
import cc.polysfaer.stochapop.ui.screens.home.HomeDestination
import cc.polysfaer.stochapop.ui.screens.reminder.EditReminderDestination
import cc.polysfaer.stochapop.ui.screens.reminder.NewReminderDestination

@Composable
fun StochaPopApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    val titleRes = when {
        currentRoute == HomeDestination.route -> HomeDestination.titleRes
        currentRoute == NewReminderDestination.route -> NewReminderDestination.titleRes
        currentRoute?.startsWith(EditReminderDestination.route) == true -> EditReminderDestination.titleRes
        else -> R.string.app_name
    }

    val navigateBackHome: ()->Unit = {
        navController.popBackStack(HomeDestination.route, false)
    }

    Scaffold(
        topBar = {
            StochaPopAppBar(
                titleRes = titleRes,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = navigateBackHome
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            navigateBackHome = navigateBackHome
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StochaPopAppBar(
    modifier: Modifier = Modifier,
    titleRes: Int,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        expandedHeight = 45.dp,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = { Text(text = stringResource(titleRes)) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.top_bar_arrow_desc)
                    )
                }
            }
        },
    )
}
