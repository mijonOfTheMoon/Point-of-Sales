package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun SubHeaderStat(label: String, value: String, modifier: Modifier = Modifier, cs: ColorScheme) {
    Box(
        modifier = modifier
            .height(91.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cs.onPrimary.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = cs.onPrimary.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = cs.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

fun abbreviateNumber(value: Double): String {
    val abs = abs(value)
    val prefix = if (value < 0) "-" else ""
    return when {
        abs >= 1_000_000_000 -> "${prefix}%.1fB".format(abs / 1_000_000_000.0).replace(".0B", "B")
        abs >= 1_000_000     -> "${prefix}%.1fM".format(abs / 1_000_000.0).replace(".0M", "M")
        abs >= 1_000         -> "${prefix}%.1fK".format(abs / 1_000.0).replace(".0K", "K")
        abs % 1.0 == 0.0     -> "${prefix}${abs.toInt()}"
        else                 -> "${prefix}$abs"
    }
}

fun abbreviateNumber(value: Long): String = abbreviateNumber(value.toDouble())
fun abbreviateNumber(value: Int): String = abbreviateNumber(value.toDouble())

fun recentTimestamp(updatedAt: String?, createdAt: String? = null, fallback: String? = null): String {
    return updatedAt ?: createdAt ?: fallback.orEmpty()
}

fun rupiahFormatter(): NumberFormat {
    return NumberFormat.getCurrencyInstance(
        Locale.Builder()
            .setLanguage("id")
            .setRegion("ID")
            .build()
    ).apply {
        maximumFractionDigits = 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSortBar(
    query: String,
    onQueryChange: (String) -> Unit,
    sortLabel: String,
    sortOptions: List<String>,
    onSortChange: (String) -> Unit,
    placeholder: String = "Search"
) {
    val cs = MaterialTheme.colorScheme
    val expanded = remember { mutableStateOf(false) }
    val fieldBorderColor = cs.outlineVariant.copy(alpha = 0.50f)
    val activeBorderColor = cs.primary.copy(alpha = 0.65f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = fieldBorderColor,
                focusedBorderColor = activeBorderColor,
                unfocusedContainerColor = cs.surface,
                focusedContainerColor = cs.surface
            )
        )

        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = it }
        ) {
            OutlinedButton(
                onClick = { expanded.value = true },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .width(124.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, if (expanded.value) activeBorderColor else fieldBorderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = cs.surface),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = cs.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    sortLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSortChange(option)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}
