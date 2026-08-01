package com.aimessagehub.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aimessagehub.app.ai.AppSettings
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.service.Permissions

data class PermissionSnapshot(
    val notification: Boolean,
    val accessibility: Boolean,
    val overlay: Boolean,
)

@Composable
fun OverviewScreen(
    permission: PermissionSnapshot,
    settings: AppSettings,
    viewModel: MainViewModel,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI 消息中枢",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "本地优先 · 白名单自动回复",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "刷新")
                }
            }
        }

        item {
            PermissionCard(
                title = "通知监听",
                enabled = permission.notification,
                icon = Icons.Outlined.VolumeUp,
                onOpen = {
                    Permissions.openNotificationSettings(context)
                },
            )
        }
        item {
            PermissionCard(
                title = "无障碍执行",
                enabled = permission.accessibility,
                icon = Icons.Outlined.AdsClick,
                onOpen = {
                    Permissions.openAccessibilitySettings(context)
                },
            )
        }
        item {
            PermissionCard(
                title = "悬浮建议",
                enabled = permission.overlay,
                icon = Icons.Outlined.ChatBubble,
                onOpen = {
                    Permissions.openOverlaySettings(context)
                },
            )
        }

        item {
            Text(
                text = "已接入 App",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(AppSource.entries.filter { it != AppSource.GENERIC }) { app ->
            CaptureAppRow(
                app = app,
                enabled = app in settings.captureApps,
                onCheckedChange = { viewModel.toggleAppCapture(app, it) },
            )
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    enabled: Boolean,
    icon: ImageVector,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (enabled) "已开启" else "去开启",
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (enabled) "●" else "○",
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (!enabled) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Outlined.AdsClick, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun CaptureAppRow(
    app: AppSource,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = app.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
