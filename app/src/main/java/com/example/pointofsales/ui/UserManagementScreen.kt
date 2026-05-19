package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.model.AdminUser
import com.example.pointofsales.viewmodel.AuthCheckState
import com.example.pointofsales.viewmodel.AuthViewModel
import com.example.pointofsales.viewmodel.UserUiState
import com.example.pointofsales.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val uiState by userViewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.checkState.collectAsStateWithLifecycle()
    val currentUserId = (authState as? AuthCheckState.Authenticated)?.id.orEmpty()
    val cs = MaterialTheme.colorScheme
    var showAddSheet by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<AdminUser?>(null) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("A-Z") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = cs.primary,
                contentColor = cs.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Add User") }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Users", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val users = (uiState as? UserUiState.Success)?.users ?: emptyList()
                val adminCount = users.count { it.role == "admin" }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubHeaderStat("Total Users", abbreviateNumber(users.size), Modifier.weight(1f), cs)
                    SubHeaderStat("Admins", abbreviateNumber(adminCount), Modifier.weight(1f), cs)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is UserUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                    is UserUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as UserUiState.Error).message, color = cs.error)
                    }
                    is UserUiState.Success -> {
                        val users = (uiState as UserUiState.Success).users
                            .filter { it.name.contains(query, true) || it.email.contains(query, true) || it.role.contains(query, true) }
                            .let { list ->
                                when (sort) {
                                    "Recent" -> list.sortedByDescending { recentTimestamp(it.updated_at, it.created_at) }
                                    "Role" -> list.sortedWith(compareBy<AdminUser> { it.role }.thenBy { it.name.lowercase() })
                                    else -> list.sortedBy { it.name.lowercase() }
                                }
                            }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                            item {
                                SearchSortBar(
                                    query = query,
                                    onQueryChange = { query = it },
                                    sortLabel = sort,
                                    sortOptions = listOf("A-Z", "Recent", "Role"),
                                    onSortChange = { sort = it },
                                    placeholder = "Search users"
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            items(users) { user ->
                                UserItem(
                                    user = user,
                                    isCurrentUser = user.id == currentUserId,
                                    onEdit = { userToEdit = it },
                                    onDelete = { userViewModel.deleteUser(it.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        UserSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name, email, password, role ->
                userViewModel.createUser(name, email, password, role)
                showAddSheet = false
            }
        )
    }

    userToEdit?.let { user ->
        UserSheet(
            user = user,
            onDismiss = { userToEdit = null },
            onConfirm = { name, email, password, role ->
                userViewModel.updateUser(user.id, name, email, password.takeIf { it.isNotBlank() }, role)
                if (user.id == currentUserId) authViewModel.refreshSessionProfile()
                userToEdit = null
            }
        )
    }
}

@Composable
private fun UserItem(
    user: AdminUser,
    isCurrentUser: Boolean,
    onEdit: (AdminUser) -> Unit,
    onDelete: (AdminUser) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = cs.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name.ifBlank { "Unnamed" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = cs.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.55f))
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = cs.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = if (isCurrentUser) "${user.role} • You" else user.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = { onEdit(user) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = cs.primary)
            }
            IconButton(onClick = { onDelete(user) }, enabled = !isCurrentUser) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = if (isCurrentUser) cs.onSurface.copy(alpha = 0.25f) else cs.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSheet(
    user: AdminUser? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "cashier") }
    var expanded by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val roles = listOf("admin", "cashier", "supervisor", "stocker")

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(if (user == null) "Add User" else "Edit User", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(if (user == null) "Password" else "New Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = role,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    roles.forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = { role = item; expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(name, email, password, role) },
                enabled = name.isNotBlank() && email.isNotBlank() && (user != null || password.isNotBlank()),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (user == null) "Create User" else "Save Changes", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
