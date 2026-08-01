package com.aimessagehub.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aimessagehub.app.domain.ContactPolicy

@Composable
fun ContactsScreen(
    contacts: List<ContactPolicy>,
    viewModel: MainViewModel,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(contacts, key = { "${it.app.name}:${it.contactId}" }) { policy ->
            ContactPolicyCard(
                policy = policy,
                onCheckedChange = { enabled ->
                    viewModel.setContactAutoReply(
                        appSource = policy.app,
                        contactId = policy.contactId,
                        contactName = policy.contactName,
                        enabled = enabled,
                    )
                },
            )
        }
        if (contacts.isEmpty()) {
            item {
                Text(
                    text = "暂无白名单联系人",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ContactPolicyCard(
    policy: ContactPolicy,
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
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = policy.contactName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = policy.app.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = policy.autoReplyEnabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

