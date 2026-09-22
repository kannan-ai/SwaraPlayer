package com.example.swaraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.data.SortOrder
import com.example.swaraplayer.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    title: String,
    showBackButton: Boolean = false,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortSelect: (SortOrder) -> Unit,
    onSettings: () -> Unit,
    onOpenMusic: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSearching) {
            // Expandable Real-Time Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    onSearchQueryChange(query)
                },
                placeholder = { Text("Search videos or folders...", color = colors.textSecondary, fontSize = 13.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        isSearching = false
                        searchQuery = ""
                        onSearchQueryChange("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Search", tint = colors.textPrimary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accentOrange,
                    unfocusedBorderColor = colors.textSecondary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            )
        } else {
            // Back button shown ONLY if inside a subfolder
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                    )
                }
            }

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )

            // Prominent Music Player Button
            if (onOpenMusic != null) {
                IconButton(onClick = onOpenMusic) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music Player",
                        tint = colors.accentOrange,
                    )
                }
            }

            // Interactive Gestures Help Guide Button
            IconButton(onClick = onHelp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Help Guide",
                    tint = colors.textPrimary,
                )
            }

            // Search Icon
            IconButton(onClick = { isSearching = true }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.textPrimary,
                )
            }

            // Sort Menu Icon & Dropdown
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort Options",
                        tint = colors.textPrimary,
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(colors.surface),
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label, color = colors.textPrimary, fontSize = 13.sp) },
                            onClick = {
                                onSortSelect(order)
                                showSortMenu = false
                            },
                        )
                    }
                }
            }

            // Settings Icon
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.textPrimary,
                )
            }
        }
    }
}