package com.nexuzstudios.qrcodescanner_quickscan.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexuzstudios.qrcodescanner_quickscan.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val vibrateOnScan by viewModel.vibrateOnScan.collectAsState()
    val beepOnScan    by viewModel.beepOnScan.collectAsState()
    val autoOpenUrl   by viewModel.autoOpenUrl.collectAsState()
    val copyOnScan    by viewModel.copyOnScan.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Scan Behaviour ──────────────────────────────────────────────
            SettingsSectionTitle("SCAN BEHAVIOUR")

            SettingsSwitchItem(
                icon = Icons.Outlined.Vibration,
                title = "Vibrate on scan",
                subtitle = "Haptic feedback when a code is detected",
                checked = vibrateOnScan,
                onCheckedChange = viewModel::setVibrateOnScan
            )
            SettingsSwitchItem(
                icon = Icons.Outlined.VolumeUp,
                title = "Beep on scan",
                subtitle = "Play sound when a code is detected",
                checked = beepOnScan,
                onCheckedChange = viewModel::setBeepOnScan
            )
            SettingsSwitchItem(
                icon = Icons.Outlined.OpenInNew,
                title = "Auto-open URLs",
                subtitle = "Automatically open links in browser",
                checked = autoOpenUrl,
                onCheckedChange = viewModel::setAutoOpenUrl
            )
            SettingsSwitchItem(
                icon = Icons.Outlined.ContentCopy,
                title = "Copy on scan",
                subtitle = "Automatically copy scanned content",
                checked = copyOnScan,
                onCheckedChange = viewModel::setCopyOnScan
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // ── App ─────────────────────────────────────────────────────────
            SettingsSectionTitle("APP")

            SettingsClickItem(
                icon = Icons.Outlined.Star,
                title = "Rate App",
                subtitle = "Love the app? Rate us 5 stars!",
                onClick = viewModel::rateApp
            )
            SettingsClickItem(
                icon = Icons.Outlined.Share,
                title = "Share App",
                subtitle = "Share with friends and family",
                onClick = viewModel::shareApp
            )
            SettingsClickItem(
                icon = Icons.Outlined.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "View our privacy policy",
                onClick = viewModel::openPrivacyPolicy
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp, end = 24.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = androidx.compose.ui.graphics.Color(0xFFCFD8DC),
                uncheckedTrackColor = androidx.compose.ui.graphics.Color(0xFF37474F),
                uncheckedBorderColor = androidx.compose.ui.graphics.Color(0xFF546E7A)
            )
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        Icon(
            Icons.Filled.ChevronRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
