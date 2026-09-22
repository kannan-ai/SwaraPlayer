package com.example.swaraplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun SelectionActionBar(
    selectedCount: Int,
    onSelectAllToggle: () -> Unit,
    onClearSelection: () -> Unit,
    onMoveToFolder: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedCount <= 0) return
    val colors = LocalAppColors.current

    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClearSelection, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "$selectedCount Selected",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSelectAllToggle, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onMoveToFolder, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Move to Folder", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onShare, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = colors.accentOrange, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.badgeRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}