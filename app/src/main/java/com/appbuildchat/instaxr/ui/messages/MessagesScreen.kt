package com.appbuildchat.instaxr.ui.messages

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.appbuildchat.instaxr.data.model.Chat
import com.appbuildchat.instaxr.data.model.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Top-level composable for the Messages feature screen
 * Uses activity-scoped ViewModel for consistency with InstaXRApp
 */
@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier
) {
    // Get activity-scoped ViewModel (same instance as InstaXRApp uses)
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity

    val viewModel: MessagesViewModel = if (activity != null) {
        hiltViewModel(viewModelStoreOwner = activity)
    } else {
        hiltViewModel()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MessagesContent(
        uiState = uiState,
        onAction = viewModel::handleAction,
        modifier = modifier
    )
}

/**
 * Internal composable for Messages screen content
 */
@Composable
internal fun MessagesContent(
    uiState: MessagesUiState,
    onAction: (MessagesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is MessagesUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is MessagesUiState.Success -> {
            // Just show chat list (spatial panels handled at app level)
            ChatListPanel(
                chats = uiState.chats,
                onChatClick = { chatId -> onAction(MessagesAction.SelectChat(chatId)) },
                modifier = modifier
            )
        }
        is MessagesUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.message}")
            }
        }
    }
}

/**
 * Two-panel spatial layout for Messages screen when a chat is selected
 * Called from InstaXRApp when in XR mode with selected chat
 */
@SuppressLint("RestrictedApi")
@Composable
fun MessagesScreenSpatialPanels(
    uiState: MessagesUiState.Success,
    onAction: (MessagesAction) -> Unit
) {
    // Animate left panel width shrinking
    val leftPanelWidth by animateDpAsState(
        targetValue = 250.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "leftPanelWidth"
    )

    // Animate alpha for chat detail panel (fade in effect)
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            animatedAlpha.animateTo(
                1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }

    SpatialRow {
        // Left panel: Chat list (shrunken)
        SpatialPanel(
            modifier = SubspaceModifier
                .width(leftPanelWidth)
                .height(700.dp),
            dragPolicy = MovePolicy(isEnabled = true),
            resizePolicy = ResizePolicy(isEnabled = false)
        ) {
            Surface {
                ChatListPanel(
                    chats = uiState.chats,
                    onChatClick = { chatId -> onAction(MessagesAction.SelectChat(chatId)) }
                )
            }
        }

        // Right panel: Chat detail (fade in)
        SpatialPanel(
            modifier = SubspaceModifier
                .width(900.dp)
                .height(700.dp),
            dragPolicy = MovePolicy(isEnabled = true),
            resizePolicy = ResizePolicy(isEnabled = true)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().alpha(animatedAlpha.value)
            ) {
                ChatDetailPanel(
                    chat = uiState.selectedChat,
                    messages = uiState.messages,
                    onClose = { onAction(MessagesAction.DeselectChat) },
                    onSendMessage = { message -> onAction(MessagesAction.SendMessage(message)) }
                )
            }
        }
    }
}

/**
 * Chat list panel showing all conversations
 */
@Composable
private fun ChatListPanel(
    chats: List<Chat>,
    onChatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Chat list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                ChatListItem(
                    chat = chat,
                    onClick = { onChatClick(chat.id) }
                )
            }
        }
    }
}

/**
 * Individual chat list item
 */
@Composable
private fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/mock_data/${chat.profileImage}")
                    .crossfade(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = "Profile picture of ${chat.displayName}",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Chat info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (chat.unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${if (chat.lastMessage.sender == "me") "You: " else ""}${chat.lastMessage.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (chat.unreadCount > 0)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Chat detail panel showing conversation with a specific user
 */
@Composable
private fun ChatDetailPanel(
    chat: Chat?,
    messages: List<Message>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onSendMessage: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var messageText by rememberSaveable { mutableStateOf("") }

    if (chat == null) return

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with user info and close button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/mock_data/${chat.profileImage}")
                            .crossfade(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = "Profile picture of ${chat.displayName}",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = chat.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${chat.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close chat"
                    )
                }
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }

        // Message input field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && onSendMessage != null) {
                            onSendMessage(messageText.trim())
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = if (messageText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual message bubble
 */
@Composable
private fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isMe = message.isMe
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isMe) 0.dp else 2.dp
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            Text(
                text = dateFormat.format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
