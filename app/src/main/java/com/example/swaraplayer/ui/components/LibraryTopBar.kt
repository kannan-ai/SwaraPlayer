package com.example.swaraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSearching) {
            // Compact Real-Time Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    onSearchQueryChange(query)
                },
                placeholder = { Text("Search videos or folders...", color = colors.textSecondary, fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            isSearching = false
                            searchQuery = ""
                            onSearchQueryChange("")
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Search", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
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
                    .height(42.dp),
            )
        } else {
            // Back button shown ONLY if inside a subfolder
            if (showBackButton) {
                IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (showBackButton) 4.dp else 0.dp),
            )

            // Compact Action Buttons
            if (onOpenMusic != null) {
                IconButton(onClick = onOpenMusic, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music Player",
                        tint = colors.accentOrange,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }

            IconButton(onClick = onHelp, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Help Guide",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(19.dp),
                )
            }

            IconButton(onClick = { isSearching = true }, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(19.dp),
                )
            }

            Box {
                IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Sort Options",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(19.dp),
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

            IconButton(onClick = onSettings, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}