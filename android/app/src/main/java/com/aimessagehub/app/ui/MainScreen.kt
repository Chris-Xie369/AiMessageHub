package com.aimessagehub.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aimessagehub.app.service.Permissions

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var permissionTick by rememberSaveable { mutableIntStateOf(0) }
    val permission = remember(context, permissionTick) {
        PermissionSnapshot(
            notification = Permissions.isNotificationListenerEnabled(context),
            accessibility = Permissions.isAccessibilityEnabled(context),
            overlay = Permissions.canDrawOverlays(context),
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "总览") },
                    label = { Text("总览") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Outlined.Chat, contentDescription = "会话") },
                    label = { Text("会话") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "白名单") },
                    label = { Text("白名单") },
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { tab = 3 },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "设置") },
                    label = { Text("设置") },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (tab) {
                0 -> OverviewScreen(
                    permission = permission,
                    settings = settings,
                    viewModel = viewModel,
                    onRefresh = { permissionTick++ },
                )
                1 -> ConversationsScreen(
                    conversations = conversations,
                    suggestions = suggestions,
                    viewModel = viewModel,
                )
                2 -> ContactsScreen(
                    contacts = contacts,
                    viewModel = viewModel,
                )
                3 -> SettingsScreen(
                    settings = settings,
                    viewModel = viewModel,
                )
            }
        }
    }
}

