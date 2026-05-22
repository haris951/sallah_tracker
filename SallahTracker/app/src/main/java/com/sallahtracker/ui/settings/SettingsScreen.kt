package com.sallahtracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallahtracker.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                SettingsHeader()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SettingsSectionTitle("APPEARANCE")
                SettingsCard {
                    ToggleSettingItem(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Switch to dark theme",
                        checked = state.isDarkMode,
                        onCheckedChange = { viewModel.onIntent(SettingsIntent.ToggleDarkMode(it)) },
                        iconColor = PrimaryGreen,
                        showThumbIcon = true
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SettingsSectionTitle("NOTIFICATIONS")
                SettingsCard {
                    ToggleSettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Prayer Notifications",
                        subtitle = "Enable prayer time reminders",
                        checked = state.isNotificationsEnabled,
                        onCheckedChange = { viewModel.onIntent(SettingsIntent.ToggleNotifications(it)) },
                        iconColor = PrimaryGreen
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SettingsSectionTitle("LOCATION")
                SettingsCard {
                    Column {
                        ToggleSettingItem(
                            icon = Icons.Outlined.LocationOn,
                            title = "Auto-detect Location",
                            subtitle = "Automatically calculate prayer times",
                            checked = state.isAutoLocationEnabled,
                            onCheckedChange = { viewModel.onIntent(SettingsIntent.ToggleAutoLocation(it)) },
                            iconColor = PrimaryGreen
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        ClickableSettingItem(
                            icon = Icons.Outlined.AccessTime,
                            title = "Calculation Method",
                            subtitle = state.calculationMethod,
                            onClick = { viewModel.onIntent(SettingsIntent.ChangeCalculationMethod) },
                            iconColor = PrimaryGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SettingsSectionTitle("ABOUT")
                SettingsCard {
                    Column {
                        InfoSettingItem(
                            icon = Icons.Outlined.Info,
                            title = "App Version",
                            subtitle = state.appVersion,
                            iconColor = PrimaryGreen
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        ClickableSettingItem(
                            icon = Icons.Outlined.HelpOutline,
                            title = "Help & Support",
                            subtitle = "Get help with the app",
                            onClick = { viewModel.onIntent(SettingsIntent.OpenHelpAndSupport) },
                            iconColor = PrimaryGreen
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        ClickableSettingItem(
                            icon = Icons.Outlined.FavoriteBorder,
                            title = "Rate Us",
                            subtitle = "Share your feedback",
                            onClick = { viewModel.onIntent(SettingsIntent.RateApp) },
                            iconColor = Color(0xFFE57373)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

            item {
                Footer()
            }
        }
    }
}

@Composable
fun SettingsHeader() {
    Column {
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Customize your prayer tracking experience",
            fontSize = 16.sp,
            color = TextLight
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextLight,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun ToggleSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color,
    showThumbIcon: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon, iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = subtitle, fontSize = 14.sp, color = TextLight)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = if (showThumbIcon) {
                {
                    Icon(
                        imageVector = if (checked) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SecondaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = PendingGrey,
                uncheckedBorderColor = Color.Transparent,
                checkedIconColor = SecondaryGreen,
                uncheckedIconColor = Color.Gray
            )
        )
    }
}

@Composable
fun ClickableSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon, iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = subtitle, fontSize = 14.sp, color = TextLight)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

@Composable
fun InfoSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon, iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = subtitle, fontSize = 14.sp, color = TextLight)
        }
    }
}

@Composable
fun SettingIcon(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BeigeBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Made with ♡ for the Muslim community",
            fontSize = 14.sp,
            color = TextLight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "May Allah accept all our prayers",
            fontSize = 14.sp,
            color = TextLight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
