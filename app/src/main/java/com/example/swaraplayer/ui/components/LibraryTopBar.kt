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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun LibraryTopBar(
    title: String,
    showBackButton: Boolean = false,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onSortSelect: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .height(36.dp), // Sleek, ultra-compact top bar height
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back button shown ONLY if inside a subfolder
        if (showBackButton) {
            IconButton(onClick = onBack, modifier = Modifier.size(30.dp)) {
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

        // Question / Help Icon
        IconButton(onClick = onHelp, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help Guide",
                tint = colors.textPrimary,
                modifier = Modifier.size(18.dp),
            )
        }

        // 3-Bars Menu Icon & Dropdown
        Box {
            IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp),
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
    }
}