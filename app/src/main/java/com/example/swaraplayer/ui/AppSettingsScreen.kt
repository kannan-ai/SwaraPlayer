package com.example.swaraplayer.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swaraplayer.player.PlayerViewModel
import com.example.swaraplayer.ui.theme.AppThemeMode
import com.example.swaraplayer.ui.theme.LocalAppColors

@Composable
fun AppSettingsScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val currentTheme by viewModel.appThemeMode.collectAsState()
    val isOledEnabled by viewModel.isOledEnabled.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    // Determine if Dark Mode is active (for disabling OLED switch in Light Mode)
    val isDarkActive = when (currentTheme) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = "Settings",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        },
        bottomBar = {
            // Pinned Footer Container (Actions & Sharing)
            Surface(
                color = colors.surface,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left Action: Check for Updates
                    Button(
                        onClick = { viewModel.checkForAppUpdates() },
                        enabled = !isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentOrange,
                            disabledContainerColor = colors.accentOrange.copy(alpha = 0.5f),
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCheckingUpdate) "Checking..." else "Check Updates",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // Right Action: App Share (OUTBOX / EXPORT)
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Download Swara Player")
                                putExtra(Intent.EXTRA_TEXT, "Experience Swara Player - Premium Media Player for Android!\nDownload APK: https://github.com/kannan-ai/SwaraPlayer/releases")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Swara Player"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.background),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = colors.accentOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share App",
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        },
        containerColor = colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top Scrollable Section: Appearance
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Accent Highlight
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = colors.accentOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance",
                            color = colors.accentOrange,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Radio Selectors: System Default, Light Mode, Dark Mode
                    AppThemeRadioRow(
                        title = "System Default",
                        description = "Follow device system theme settings",
                        isSelected = currentTheme == AppThemeMode.SYSTEM,
                        onClick = { viewModel.appThemeMode.value = AppThemeMode.SYSTEM },
                    )

                    AppThemeRadioRow(
                        title = "Light Mode",
                        description = "Clean white surface with high contrast text",
                        isSelected = currentTheme == AppThemeMode.LIGHT,
                        onClick = { viewModel.appThemeMode.value = AppThemeMode.LIGHT },
                    )

                    AppThemeRadioRow(
                        title = "Dark Mode",
                        description = "Modern dark grey theme",
                        isSelected = currentTheme == AppThemeMode.DARK,
                        onClick = { viewModel.appThemeMode.value = AppThemeMode.DARK },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colors.textSecondary.copy(alpha = 0.2f),
                    )

                    // OLED Pure Black Toggle Switch (Disabled in Light Mode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pure OLED Black",
                                color = if (isDarkActive) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkActive) "True pitch black surfaces (#000000) to save battery" else "Requires Dark Mode or System Dark",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                            )
                        }

                        Switch(
                            checked = isOledEnabled && isDarkActive,
                            enabled = isDarkActive,
                            onCheckedChange = { viewModel.isOledEnabled.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accentOrange,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.background,
                            ),
                        )
                    }
                }
            }

            // Version Information Card
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Swara Player",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version v0.0.2 • Open Source GitHub Edition",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppThemeRadioRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                color = colors.textSecondary,
                fontSize = 11.sp,
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accentOrange,
                unselectedColor = colors.textSecondary,
            ),
        )
    }
}