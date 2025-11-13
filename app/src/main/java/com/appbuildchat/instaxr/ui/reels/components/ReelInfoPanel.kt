package com.appbuildchat.instaxr.ui.reels.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appbuildchat.instaxr.data.model.Reel

@Composable
fun ReelInfoPanel(
    reel: Reel?,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    onCommentLikeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        if (reel != null) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Reel Information Section
                ReelInfoSection(
                    reel = reel,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // Comments Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Comments (${reel.comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (reel.comments.isEmpty()) {
                            item {
                                Text(
                                    text = "No comments yet. Be the first to comment!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        } else {
                            items(reel.comments) { comment ->
                                CommentItem(
                                    comment = comment,
                                    onLikeClick = onCommentLikeClick
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Action Buttons Section
                ActionButtons(
                    isLiked = reel.isLiked,
                    onLikeClick = onLikeClick,
                    onShareClick = onShareClick,
                    onMoreClick = onMoreClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Loading state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Loading reel information...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
