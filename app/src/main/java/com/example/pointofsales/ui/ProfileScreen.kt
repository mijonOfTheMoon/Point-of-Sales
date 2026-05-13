package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.viewmodel.AuthCheckState
import com.example.pointofsales.viewmodel.AuthUiState
import com.example.pointofsales.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val authState by authViewModel.checkState.collectAsStateWithLifecycle()
    val uiState   by authViewModel.uiState.collectAsStateWithLifecycle()

    val authenticated = authState as? AuthCheckState.Authenticated
    val role  = authenticated?.role  ?: ""
    val email = authenticated?.email ?: ""
    val name  = authenticated?.name  ?: ""
    val cs    = MaterialTheme.colorScheme

    var showEditSheet by remember { mutableStateOf(false) }

    // Handle uiState feedback (errors / success) and reset it
    var snackMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is AuthUiState.Success -> { snackMessage = s.message; authViewModel.resetUiState() }
            is AuthUiState.Error   -> { snackMessage = s.message; authViewModel.resetUiState() }
            else -> {}
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackMessage) {
        snackMessage?.let { snackbarHostState.showSnackbar(it); snackMessage = null }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            // ── Dark header ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Back button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = cs.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = cs.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Avatar + identity block
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(cs.inversePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (if (name.isNotBlank()) name else role).take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (name.isNotBlank()) name else role.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onPrimary
                        )
                        Text(
                            text = role.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onPrimary.copy(alpha = 0.6f)
                        )
                        if (email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onPrimary.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }

            // ── Light body ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                // ACCOUNT section
                SectionLabel("ACCOUNT")

                ProfileActionCard(
                    icon     = Icons.Default.Badge,
                    iconBg   = cs.primaryContainer,
                    iconTint = cs.primary,
                    label    = "Role",
                    value    = role.replaceFirstChar { it.uppercase() }
                )

                if (email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileActionCard(
                        icon     = Icons.Default.Email,
                        iconBg   = cs.primaryContainer,
                        iconTint = cs.inversePrimary,
                        label    = "Email",
                        value    = email
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                ProfileActionCard(
                    icon     = Icons.Default.Person,
                    iconBg   = cs.primaryContainer,
                    iconTint = cs.primary,
                    label    = "Display Name",
                    value    = if (name.isNotBlank()) name else "-"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ACTIONS section
                SectionLabel("ACTIONS")

                // Edit Profile card
                Card(
                    onClick = { showEditSheet = true },
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cs.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint     = cs.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Edit Profile",  style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = cs.primary)
                            Text("Update name, email or password", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.45f))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = cs.onSurface.copy(alpha = 0.25f), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                SectionLabel("SESSION")

                // Sign out card
                Card(
                    onClick = { authViewModel.signOut(); onLogout() },
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cs.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint     = cs.error
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sign Out",    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = cs.error)
                            Text("End session", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.45f))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = cs.onSurface.copy(alpha = 0.25f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // ── Edit Profile bottom sheet ────────────────────────────────────────────
    if (showEditSheet) {
        EditProfileSheet(
            currentName  = name,
            currentEmail = email,
            isLoading    = uiState is AuthUiState.Loading,
            onDismiss    = { showEditSheet = false },
            onSave       = { newName, newEmail, newPassword ->
                authViewModel.updateProfile(
                    newName     = newName.takeIf { it.isNotBlank() && it != name },
                    newEmail    = newEmail.takeIf { it.isNotBlank() && it != email },
                    newPassword = newPassword.takeIf { it.isNotBlank() }
                )
                showEditSheet = false
            }
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName:  String,
    currentEmail: String,
    isLoading:    Boolean,
    onDismiss:    () -> Unit,
    onSave:       (name: String, email: String, password: String) -> Unit
) {
    var name     by remember { mutableStateOf(currentName)  }
    var email    by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = cs.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Leave a field blank to keep it unchanged.", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPass) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick  = { onSave(name, email, password) },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = cs.onPrimary, strokeWidth = 2.dp)
                else Text("Save Changes", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text   = text,
        style  = MaterialTheme.typography.labelSmall,
        color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileActionCard(
    icon:     ImageVector,
    iconBg:   Color,
    iconTint: Color,
    label:    String,
    value:    String
) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp), tint = iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurface.copy(alpha = 0.45f))
                Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = cs.primary)
            }
        }
    }
}
