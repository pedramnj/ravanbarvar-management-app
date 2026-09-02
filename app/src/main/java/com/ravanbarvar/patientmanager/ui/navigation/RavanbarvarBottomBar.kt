package com.ravanbarvar.patientmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.ui.theme.SagePrimary

private data class BottomItem(val route: String, val icon: ImageVector, val labelRes: Int)

private val items = listOf(
    BottomItem(Routes.Dashboard, Icons.Filled.Home, R.string.nav_dashboard),
    BottomItem(Routes.Calendar, Icons.Filled.CalendarMonth, R.string.nav_calendar),
    BottomItem(Routes.Patients, Icons.Filled.Groups, R.string.nav_patients),
    BottomItem(Routes.Settings, Icons.Filled.Settings, R.string.nav_settings)
)

@Composable
fun RavanbarvarBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                label = { Text(stringResource(item.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SagePrimary,
                    selectedTextColor = SagePrimary,
                    indicatorColor = SagePrimary.copy(alpha = 0.16f)
                )
            )
        }
    }
}
