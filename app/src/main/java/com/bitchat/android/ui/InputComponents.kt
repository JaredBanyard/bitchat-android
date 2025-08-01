package com.bitchat.android.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

/**
 * Input components for ChatScreen
 * Extracted from ChatScreen.kt for better organization
 */

/**
 * VisualTransformation that styles slash commands with background and color
 * while preserving cursor positioning and click handling
 */
class SlashCommandVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val slashCommandRegex = Regex("(/\\w+)(?=\\s|$)")
        val annotatedString = buildAnnotatedString {
            var lastIndex = 0
            
            slashCommandRegex.findAll(text.text).forEach { match ->
                // Add text before the match
                if (match.range.first > lastIndex) {
                    append(text.text.substring(lastIndex, match.range.first))
                }
                
                // Add the styled slash command
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF00FF7F), // Bright green
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        background = Color(0xFF2D2D2D) // Dark gray background
                    )
                ) {
                    append(match.value)
                }
                
                lastIndex = match.range.last + 1
            }
            
            // Add remaining text
            if (lastIndex < text.text.length) {
                append(text.text.substring(lastIndex))
            }
        }
        
        return TransformedText(
            text = annotatedString,
            offsetMapping = OffsetMapping.Identity
        )
    }
}

@Composable
fun MessageInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    selectedPrivatePeer: String?,
    currentChannel: String?,
    nickname: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isFocused = remember { mutableStateOf(false) }
    val hasText = value.text.isNotBlank() // Check if there's text for send button state
    
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp), // Reduced padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text input with placeholder
        Box(
            modifier = Modifier.weight(1f)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { 
                    if (hasText) onSend() // Only send if there's text
                }),
                visualTransformation = SlashCommandVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused.value = focusState.isFocused
                    }
            )
            
            // Show placeholder when there's no text
            if (value.text.isEmpty()) {
                Text(
                    text = "type a message...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = colorScheme.onSurface.copy(alpha = 0.5f), // Muted grey
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
        
        // Send button with enabled/disabled state
        IconButton(
            onClick = { if (hasText) onSend() }, // Only execute if there's text
            enabled = hasText, // Enable only when there's text
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = if (!hasText) {
                            // Disabled state - muted grey
                            colorScheme.onSurface.copy(alpha = 0.3f)
                        } else if (selectedPrivatePeer != null || currentChannel != null) {
                            // Orange for both private messages and channels when enabled
                            Color(0xFFFF9500).copy(alpha = 0.75f)
                        } else if (colorScheme.background == Color.Black) {
                            Color(0xFF00FF00).copy(alpha = 0.75f) // Bright green for dark theme
                        } else {
                            Color(0xFF008000).copy(alpha = 0.75f) // Dark green for light theme
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Send message",
                    modifier = Modifier.size(20.dp),
                    tint = if (!hasText) {
                        // Disabled state - muted grey icon
                        colorScheme.onSurface.copy(alpha = 0.5f)
                    } else if (selectedPrivatePeer != null || currentChannel != null) {
                        // Black arrow on orange for both private and channel modes
                        Color.Black
                    } else if (colorScheme.background == Color.Black) {
                        Color.Black // Black arrow on bright green in dark theme
                    } else {
                        Color.White // White arrow on dark green in light theme
                    }
                )
            }
        }
    }
}

@Composable
fun CommandSuggestionsBox(
    suggestions: List<CommandSuggestion>,
    onSuggestionClick: (CommandSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = modifier
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(vertical = 8.dp)
    ) {
        suggestions.forEach { suggestion: CommandSuggestion ->
            CommandSuggestionItem(
                suggestion = suggestion,
                onClick = { onSuggestionClick(suggestion) }
            )
        }
    }
}

@Composable
fun CommandSuggestionItem(
    suggestion: CommandSuggestion,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .background(Color.Gray.copy(alpha = 0.1f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show all aliases together
        val allCommands = if (suggestion.aliases.isNotEmpty()) {
            listOf(suggestion.command) + suggestion.aliases
        } else {
            listOf(suggestion.command)
        }
        
        Text(
            text = allCommands.joinToString(", "),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            color = colorScheme.primary,
            fontSize = 11.sp
        )
        
        // Show syntax if any
        suggestion.syntax?.let { syntax ->
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = syntax,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Show description
        Text(
            text = suggestion.description,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MessageInputPreview() {
    MaterialTheme {
        MessageInput(
            value = TextFieldValue("Hello /world command"),
            onValueChange = {},
            onSend = {},
            selectedPrivatePeer = null,
            currentChannel = null,
            nickname = "User1"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageInputPrivatePreview() {
    MaterialTheme {
        MessageInput(
            value = TextFieldValue("Private message"),
            onValueChange = {},
            onSend = {},
            selectedPrivatePeer = "User2",
            currentChannel = null,
            nickname = "User1"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageInputChannelPreview() {
    MaterialTheme {
        MessageInput(
            value = TextFieldValue("Channel message"),
            onValueChange = {},
            onSend = {},
            selectedPrivatePeer = null,
            currentChannel = "#general",
            nickname = "User1"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageInputEmptyPreview() {
    MaterialTheme {
        MessageInput(
            value = TextFieldValue(""),
            onValueChange = {},
            onSend = {},
            selectedPrivatePeer = null,
            currentChannel = null,
            nickname = "User1"
        )
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
fun CommandSuggestionsBoxPreview() {
    MaterialTheme {
        CommandSuggestionsBox(
            suggestions = listOf(
                CommandSuggestion("/join", listOf("/j"), "<#channel>", "Join a channel"),
                CommandSuggestion("/nick", listOf(), "<new_nickname>", "Change your nickname"),
                CommandSuggestion("/msg", listOf("/m", "/query"), "<nickname> <message>", "Send a private message")
            ),
            onSuggestionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommandSuggestionItemPreview() {
    MaterialTheme {
        CommandSuggestionItem(
            suggestion = CommandSuggestion("/help", listOf("/h"), null, "Show help information for commands"),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommandSuggestionItemWithSyntaxPreview() {
    MaterialTheme {
        CommandSuggestionItem(
            suggestion = CommandSuggestion("/kick", listOf(), "<nickname> [reason]", "Kick a user from the channel"),
            onClick = {}
        )
    }
}

