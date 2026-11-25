package com.doomscrolljail.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.doomscrolljail.app.data.BlockerPrefs
import com.doomscrolljail.app.service.ShortFormBlockerService
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(prefs: BlockerPrefs) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var serviceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var blockShorts by remember { mutableStateOf(prefs.blockShorts) }
    var blockReels by remember { mutableStateOf(prefs.blockReels) }
    var blockTikTok by remember { mutableStateOf(prefs.blockTikTok) }
    var allowChatVideos by remember { mutableStateOf(prefs.allowChatVideos) }
    var tamperGuard by remember { mutableStateOf(prefs.settingsTamperGuard) }

    var blockedCount by remember { mutableIntStateOf(prefs.blockedCount) }
    var chatAllowedCount by remember { mutableIntStateOf(prefs.chatAllowedCount) }
    var isUnlocked by remember { mutableStateOf(prefs.isCurrentlyUnlocked()) }
    var unlockRemainingSec by remember { mutableIntStateOf(0) }

    var pendingGauntletAction by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceEnabled = isAccessibilityServiceEnabled(context)
                blockedCount = prefs.blockedCount
                chatAllowedCount = prefs.chatAllowedCount
                isUnlocked = prefs.isCurrentlyUnlocked()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isUnlocked) {
        while (prefs.isCurrentlyUnlocked()) {
            isUnlocked = true
            unlockRemainingSec = ((prefs.unlockedUntilMillis - System.currentTimeMillis()) / 1000L)
                .toInt()
                .coerceAtLeast(0)
            delay(1000L)
        }
        isUnlocked = false
        unlockRemainingSec = 0
    }

    val activeGauntlet = pendingGauntletAction
    if (activeGauntlet != null) {
        GauntletScreen(
            actionDescription = activeGauntlet.first,
            onCompleted = {
                prefs.grantTemporaryUnlock()
                isUnlocked = true
                activeGauntlet.second.invoke()
                pendingGauntletAction = null
            },
            onCancelled = {
                pendingGauntletAction = null
            }
        )
        return
    }

    fun requestProtectedChange(description: String, turningOffProtection: Boolean, applyChange: () -> Unit) {
        if (!turningOffProtection || prefs.isCurrentlyUnlocked()) {
            applyChange()
        } else {
            pendingGauntletAction = description to applyChange
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DoomscrollJail",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Feeds blocked. Chat DMs allowed. Zero mercy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (serviceEnabled) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (serviceEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (serviceEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (serviceEnabled) {
                                "Accessibility Blocker Active"
                            } else {
                                "Accessibility Service Disabled"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (serviceEnabled) {
                            "DoomscrollJail is actively guarding YouTube, Instagram, and TikTok."
                        } else {
                            "Grant Accessibility permission so DoomscrollJail can detect Shorts, Reels, and TikTok feeds."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (serviceEnabled && tamperGuard && !prefs.isCurrentlyUnlocked()) {
                                pendingGauntletAction = "Open Accessibility Settings" to {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                            } else {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (serviceEnabled) "Open Accessibility Settings" else "Enable Accessibility Service"
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isUnlocked) "Temporarily Unlocked" else "Gauntlet Lock Armed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isUnlocked) {
                                    "Auto-relocks in ${unlockRemainingSec / 60}m ${unlockRemainingSec % 60}s"
                                } else {
                                    "Turning off any blocker requires 12 timed prompts"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isUnlocked) {
                        TextButton(onClick = {
                            prefs.relockImmediately()
                            isUnlocked = false
                        }) {
                            Text("Re-lock Now")
                        }
                    } else {
                        OutlinedButton(onClick = {
                            pendingGauntletAction = "Unlock Settings for 5 Minutes" to {}
                        }) {
                            Text("Unlock")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "$blockedCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Feeds Blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "$chatAllowedCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Chat Videos Allowed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Blocked Feeds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ToggleRow(
                        title = "YouTube Shorts",
                        subtitle = "Blocks Shorts tab & shelf while keeping regular videos",
                        checked = blockShorts,
                        onCheckedChange = { newVal ->
                            requestProtectedChange("Disable YouTube Shorts Blocker", !newVal) {
                                blockShorts = newVal
                                prefs.blockShorts = newVal
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    ToggleRow(
                        title = "Instagram Reels",
                        subtitle = "Blocks Reels tab & feed while keeping DMs, Stories & Posts",
                        checked = blockReels,
                        onCheckedChange = { newVal ->
                            requestProtectedChange("Disable Instagram Reels Blocker", !newVal) {
                                blockReels = newVal
                                prefs.blockReels = newVal
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    ToggleRow(
                        title = "TikTok Feed",
                        subtitle = "Blocks For You / Following feeds while keeping Inbox DMs",
                        checked = blockTikTok,
                        onCheckedChange = { newVal ->
                            requestProtectedChange("Disable TikTok Blocker", !newVal) {
                                blockTikTok = newVal
                                prefs.blockTikTok = newVal
                            }
                        }
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Smart Exceptions & Anti-Tamper",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ToggleRow(
                        title = "Allow Videos Opened from Chats",
                        subtitle = "Permits 1 video when opened from DMs/WhatsApp/Telegram; blocks on swipe",
                        checked = allowChatVideos,
                        onCheckedChange = { newVal ->
                            allowChatVideos = newVal
                            prefs.allowChatVideos = newVal
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    ToggleRow(
                        title = "Settings Tamper Guard",
                        subtitle = "Backs out of DoomscrollJail's Android Accessibility Settings unless unlocked",
                        checked = tamperGuard,
                        onCheckedChange = { newVal ->
                            requestProtectedChange("Disable Settings Tamper Guard", !newVal) {
                                tamperGuard = newVal
                                prefs.settingsTamperGuard = newVal
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponent = ComponentName(context, ShortFormBlockerService::class.java)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledService = ComponentName.unflattenFromString(componentNameString)
        if (enabledService == expectedComponent) {
            return true
        }
    }
    return false
}
