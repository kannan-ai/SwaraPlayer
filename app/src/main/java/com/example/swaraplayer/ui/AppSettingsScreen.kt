package com.example.swaraplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val colors = LocalAppColors.current
    val currentTheme by viewModel.appThemeMode.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                    )
                }
                Text(
                    text = "Settings",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Theme Options Card
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = colors.accentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Theme",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    AppThemeOptionRow(
                        title = "Light Theme",
                        description = "Clean white surface with high contrast text",
                        isSelected = currentTheme == AppThemeMode.LIGHT,
                        onClick = { viewModel.appThemeMode.value = AppThemeMode.LIGHT },
                    )

                    AppThemeOptionRow(
                        title = "Dark Theme",
                        description = "Modern dark grey theme",
                        isSelected = currentTheme == AppThemeMode.DARK,
                        onClick = { viewModel.appThemeMode.value = AppThemeMode.DARK },
                    )

                    AppThemeOptionRow(
                        title = "OLED Pure Black Theme",
                        description = "True #000000 AMOLED black theme (Saves battery)",
                        isSelected = currentTheme == AppThemeMode.OLED,
                        onClick = { viewModel.appThemeMode.value = AppThemeMode.OLED },
                    )
                }
            }

            // Updates Section Card
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = colors.accentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "App Updates",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text = "Version: v0.0.1 (GitHub: kannan-ai/SwaraPlayer)",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    Button(
                        onClick = { viewModel.checkForAppUpdates() },
                        enabled = !isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCheckingUpdate) "Checking Updates..." else "Check for Updates",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppThemeOptionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
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