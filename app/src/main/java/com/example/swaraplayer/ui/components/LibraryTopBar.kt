package com.example.swaraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun LibraryTopBar(
    title: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onHelp: () -> Unit,
    onSearch: () -> Unit,
    onSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
        }

        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )

        IconButton(onClick = onHome) {
            Icon(Icons.Default.Home, contentDescription = "Home", tint = colors.textPrimary)
        }
        IconButton(onClick = onHelp) {
            Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "Help", tint = colors.textPrimary)
        }
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.textPrimary)
        }
        IconButton(onClick = onSort) {
            Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = colors.textPrimary)
        }
    }
}