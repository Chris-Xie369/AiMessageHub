package com.aimessagehub.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aimessagehub.app.ai.AppSettings
import com.aimessagehub.app.domain.AppSource
import com.aimessagehub.app.domain.ExecutionPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    viewModel: MainViewModel,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "AI 服务",
                style = MaterialTheme.typography.titleMedium,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = settings.baseUrl,
                        onValueChange = {
                            val newValue = it
                            viewModel.updateSettings { current -> current.copy(baseUrl = newValue) }
                        },
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.apiKey,
                        onValueChange = {
                            viewModel.updateSettings { current -> current.copy(apiKey = it) }
                        },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.model,
                        onValueChange = {
                            viewModel.updateSettings { current -> current.copy(model = it) }
                        },
                        label = { Text("模型") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = settings.temperature.toString(),
                            onValueChange = {
                                viewModel.updateSettings { current ->
                                    current.copy(temperature = it.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            label = { Text("Temperature") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = settings.maxTokens.toString(),
                            onValueChange = {
                                viewModel.updateSettings { current ->
                                    current.copy(maxTokens = it.toIntOrNull() ?: 0)
                                }
                            },
                            label = { Text("Max Tokens") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    OutlinedTextField(
                        value = settings.persona,
                        onValueChange = {
                            viewModel.updateSettings { current -> current.copy(persona = it) }
                        },
                        label = { Text("回复风格") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.instructions,
                        onValueChange = {
                            viewModel.updateSettings { current -> current.copy(instructions = it) }
                        },
                        label = { Text("额外要求") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Text(
                text = "执行策略",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExecutionPolicy.entries.forEach { policy ->
                    FilterChip(
                        selected = settings.defaultPolicy == policy,
                        onClick = { viewModel.setDefaultPolicy(policy) },
                        label = { Text(policy.label()) },
                    )
                }
            }
        }

        item {
            Text(
                text = "消息采集",
                style = MaterialTheme.typography.titleMedium,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    AppSource.entries.filter { it != AppSource.GENERIC }.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = app.displayName,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = app in settings.captureApps,
                                onCheckedChange = { viewModel.toggleAppCapture(app, it) },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "群聊",
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = settings.groupChatsEnabled,
                            onCheckedChange = {
                                viewModel.updateSettings { current ->
                                    current.copy(groupChatsEnabled = it)
                                }
                            },
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "悬浮建议",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Switch(
                    checked = settings.bubbleEnabled,
                    onCheckedChange = { viewModel.setBubbleEnabled(it) },
                )
            }
        }
    }
}

private fun ExecutionPolicy.label(): String = when (this) {
    ExecutionPolicy.SUGGEST -> "仅建议"
    ExecutionPolicy.ONE_TAP -> "一键发送"
    ExecutionPolicy.AUTO_WHITELIST -> "白名单自动"
}
