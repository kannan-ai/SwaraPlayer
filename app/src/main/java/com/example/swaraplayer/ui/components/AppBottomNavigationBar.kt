package com.example.swaraplayer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.ui.theme.LocalAppColors

enum class AppNavTab(val label: String, val icon: ImageVector) {
    LOCAL("Local", Icons.Default.Folder),
    MUSIC("Music", Icons.Default.MusicNote),
    SEARCH("Search", Icons.Default.Search),
    SETTINGS("Settings", Icons.Default.Settings),
}

@Composable
fun AppBottomNavigationBar(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        AppNavTab.entries.forEach { tab ->
            val isSelected = currentTab == tab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) colors.accentOrange else colors.textSecondary,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        color = if (isSelected) colors.accentOrange else colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = colors.accentOrange.copy(alpha = 0.15f),
                ),
            )
        }
    }
}