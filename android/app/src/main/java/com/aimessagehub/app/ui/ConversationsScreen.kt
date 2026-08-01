package com.aimessagehub.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aimessagehub.app.domain.ChatMessage
import com.aimessagehub.app.domain.Conversation
import com.aimessagehub.app.domain.MessageDirection
import com.aimessagehub.app.domain.SuggestionStatus
import com.aimessagehub.app.domain.UiSuggestion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsScreen(
    conversations: List<Conversation>,
    suggestions: List<UiSuggestion>,
    viewModel: MainViewModel,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = conversations.firstOrNull { it.id == selectedId }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(conversations, key = { it.id }) { conversation ->
            ConversationCard(
                conversation = conversation,
                suggestionCount = suggestions.count { it.conversationId == conversation.id },
                onClick = { selectedId = conversation.id },
            )
        }
        if (conversations.isEmpty()) {
            item {
                Text(
                    text = "暂无会话",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (selected != null) {
        ConversationDetailDialog(
            conversation = selected,
            viewModel = viewModel,
            onDismiss = { selectedId = null },
        )
    }
}

@Composable
private fun ConversationCard(
    conversation: Conversation,
    suggestionCount: Int,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${conversation.app.displayName} · ${conversation.lastPreview}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                )
                if (suggestionCount > 0) {
                    Text(
                        text = "$suggestionCount 条建议",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationDetailDialog(
    conversation: Conversation,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val messages by viewModel.messagesFor(conversation.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val conversationSuggestions = remember(viewModel.suggestions.value, conversation.id) {
        viewModel.suggestions.value
            .filter { it.conversationId == conversation.id }
            .takeLast(4)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conversation.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${conversation.app.displayName} · ${messages.size} 条",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "关闭")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageRow(message)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                if (conversationSuggestions.isEmpty()) {
                    Text(
                        text = "暂无建议",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    conversationSuggestions.forEach { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            viewModel = viewModel,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { viewModel.requestSuggestion(conversation.id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("生成建议")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteConversation(conversation.id)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("删除会话")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageRow(message: ChatMessage) {
    val isOutgoing = message.direction == MessageDirection.OUTGOING
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOutgoing) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: UiSuggestion,
    viewModel: MainViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = when (suggestion.status) {
                    SuggestionStatus.GENERATING -> "生成中"
                    SuggestionStatus.READY -> "建议"
                    SuggestionStatus.ERROR -> "生成失败"
                    SuggestionStatus.SENDING -> "发送中"
                    SuggestionStatus.SENT -> "已发送"
                    SuggestionStatus.IGNORED -> "已忽略"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (suggestion.error != null) {
                Text(
                    text = suggestion.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            suggestion.variants.forEachIndexed { index, variant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = variant,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.sendSuggestion(suggestion.id, index) },
                        enabled = suggestion.status == SuggestionStatus.READY,
                    ) {
                        Text("发送")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = { viewModel.copySuggestion(variant) },
                        enabled = suggestion.status == SuggestionStatus.READY,
                    ) {
                        Text("复制")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.cancelAutoReply(suggestion.id)
                        viewModel.ignoreSuggestion(suggestion.id)
                    },
                ) {
                    Text("忽略")
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
