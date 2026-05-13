package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pointofsales.viewmodel.AuthCheckState
import com.example.pointofsales.viewmodel.AuthViewModel


@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val authState by authViewModel.checkState.collectAsState()
    val authenticated = authState as? AuthCheckState.Authenticated

    val role  = authenticated?.role  ?: ""
    val email = authenticated?.email ?: ""
    val cs    = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Dark header ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.primary)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 20.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

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
                        text = role.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = role.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onPrimary
                    )
                    if (email.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onPrimary.copy(alpha = 0.55f)
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
            Text(
                text = "ACCOUNT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = cs.onSurface.copy(alpha = 0.45f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ProfileActionCard(
                icon      = Icons.Default.Badge,
                iconBg    = cs.surface,
                iconTint  = cs.primary,
                label     = "Role",
                value     = role.replaceFirstChar { it.uppercase() }
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SESSION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = cs.onSurface.copy(alpha = 0.45f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Sign-out card
            Card(
                onClick = {
                    authViewModel.signOut()
                    onLogout()
                },
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = cs.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cs.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint     = cs.error
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sign Out",    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = cs.error)
                        Text("End session", fontSize = 11.sp, color = cs.onSurface.copy(alpha = 0.45f))
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = cs.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
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
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = cs.onSurface.copy(alpha = 0.45f))
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = cs.primary)
            }
        }
    }
}
