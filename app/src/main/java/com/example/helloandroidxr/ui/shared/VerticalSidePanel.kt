package com.example.helloandroidxr.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Reusable vertical side panel component
 * Can be used across multiple screens for navigation or action buttons
 */
@Composable
fun VerticalSidePanel(
    items: List<SidePanelItem>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEach { item ->
            IconButton(
                onClick = { onItemClick(item.id) }
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.contentDescription,
                    tint = if (item.isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (item != items.last()) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Data class representing an item in the side panel
 */
data class SidePanelItem(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String,
    val isSelected: Boolean = false
)
