package com.yadro.internship.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yadro.internship.R
import com.yadro.internship.data.Contact

@Composable
fun ContactsList(contacts: List<Contact>) {
    val context = LocalContext.current
    if (contacts.isEmpty()) {
        Text(
            text = "Контакты отсутствуют",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    } else {
        LazyColumn {
            contacts
                .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
                .toSortedMap()
                .forEach { (letter, group) ->
                    item {
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                    items(group) { contact ->
                        ContactListItem(contact)
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }
        }
    }
}

@Composable
fun ContactListItem(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        if (contact.photoUri != null) {
            AsyncImage(
                model = contact.photoUri,
                contentDescription = "Фото контакта",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                placeholder = painterResource(R.drawable.ic_launcher_background)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                val initials = contact.name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                    .take(2)
                    .joinToString("")
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            contact.phone?.let {
                Row {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getPhoneTypeLabel(contact.phoneType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getPhoneTypeLabel(type: String?): String {
    return when (type?.toIntOrNull()) {
        android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Мобильный"
        android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Домашний"
        android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Рабочий"
        android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "Основной"
        else -> ""
    }
}