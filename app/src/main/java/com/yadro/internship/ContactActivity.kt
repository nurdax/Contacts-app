package com.yadro.internship

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yadro.internship.data.Contact
import com.yadro.internship.data.ContactRepository
import com.yadro.internship.service.ContactService
import com.yadro.internship.ui.ContactsList
import com.yadro.internship.ui.theme.ContactsManagerTheme
import kotlinx.coroutines.launch

class ContactActivity : ComponentActivity() {
    private var contactService: IContactService? = null
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactsManagerTheme {
                ContactsScreen()
            }
        }
    }

    @Composable
    fun ContactsScreen() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        val repository = remember { ContactRepository() }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.READ_CONTACTS] == true) {
                contacts = repository.getContacts(context)
            }
            if (permissions[Manifest.permission.WRITE_CONTACTS] == true) {
                scope.launch {
                    isLoading = true
                    contactService?.deleteDuplicateContacts(object : IOperationCallback.Stub() {
                        override fun onOperationCompleted(status: Int, message: String?) {
                            scope.launch {
                                statusMessage = when (status) {
                                    0 -> message ?: "Дубликаты успешно удалены"
                                    1 -> message ?: "Дубликаты не найдены"
                                    else -> message ?: "Произошла ошибка"
                                }
                                if (status == 0) {
                                    contacts = repository.getContacts(context)
                                }
                                isLoading = false
                            }
                        }
                    })
                }
            }
        }


        LaunchedEffect(Unit) {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
            } else {
                contacts = repository.getContacts(context)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_CONTACTS)
            }
            if (permissionsToRequest.isNotEmpty()) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            }

            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    contactService = IContactService.Stub.asInterface(service)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    contactService = null
                }
            }
            val intent = Intent(context, ContactService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        DisposableEffect(Unit) {
            onDispose {
                if (::serviceConnection.isInitialized) {
                    context.unbindService(serviceConnection)
                }
            }
        }


        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .safeDrawingPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                AnimatedVisibility(visible = statusMessage != null) {
                    statusMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        LaunchedEffect(it) {
                            kotlinx.coroutines.delay(3000)
                            statusMessage = null
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    ContactsList(contacts = contacts)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                            scope.launch {
                                isLoading = true
                                contactService?.deleteDuplicateContacts(object : IOperationCallback.Stub() {
                                    override fun onOperationCompleted(status: Int, message: String?) {
                                        scope.launch {
                                            statusMessage = when (status) {
                                                0 -> message ?: "Дубликаты успешно удалены"
                                                1 -> message ?: "Дубликаты не найдены"
                                                else -> message ?: "Произошла ошибка"
                                            }
                                            if (status == 0) {
                                                contacts = repository.getContacts(context)
                                            }
                                            isLoading = false
                                        }
                                    }
                                })
                            }
                        } else {
                            permissionLauncher.launch(arrayOf(Manifest.permission.WRITE_CONTACTS))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .safeContentPadding(),
                    enabled = !isLoading
                ) {
                    Text("Удалить одинаковые контакты")
                }
            }
        }
    }
}