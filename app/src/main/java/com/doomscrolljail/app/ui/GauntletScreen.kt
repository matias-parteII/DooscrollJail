package com.doomscrolljail.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

data class GauntletStep(
    val title: String,
    val question: String,
    val subtext: String,
    val waitSeconds: Int,
    val confirmLabel: String,
    val abortLabel: String,
    val requiredPhrase: String? = null,
    val flipButtons: Boolean = false
)

val DEFAULT_GAUNTLET_STEPS = listOf(
    GauntletStep(
        title = "Interrogation 1 of 12",
        question = "Are you sure you want to disable protection?",
        subtext = "You literally installed this app to stop yourself from doing what you're about to do.",
        waitSeconds = 6,
        confirmLabel = "Yes, I'm sure",
        abortLabel = "Nevermind, keep me safe"
    ),
    GauntletStep(
        title = "Interrogation 2 of 12",
        question = "Are you 100% sure?",
        subtext = "Not 99%. Not 'just checking one thing'. 100% sure you want the slot machine back?",
        waitSeconds = 8,
        confirmLabel = "Yes, 100% sure",
        abortLabel = "Abort & stay focused",
        flipButtons = true
    ),
    GauntletStep(
        title = "Interrogation 3 of 12",
        question = "Are you 1000% sure? Really?",
        subtext = "Think about how empty you felt after the last 45-minute vertical video binge.",
        waitSeconds = 10,
        confirmLabel = "I still want to turn it off",
        abortLabel = "You're right, cancel"
    ),
    GauntletStep(
        title = "Interrogation 4 of 12",
        question = "Quick sanity check: What are you avoiding right now?",
        subtext = "Work? Sleep? Studying? Staring at the ceiling with your own thoughts?",
        waitSeconds = 12,
        confirmLabel = "I don't care, let me scroll",
        abortLabel = "Fine, I'll go do the thing",
        flipButtons = true
    ),
    GauntletStep(
        title = "Interrogation 5 of 12",
        question = "Do you honestly believe you'll stop after 'just 5 minutes'?",
        subtext = "Historical data says 'just 5 minutes' is a statistical hallucination.",
        waitSeconds = 12,
        confirmLabel = "Yes, I have iron willpower (lie)",
        abortLabel = "Keep blocker active"
    ),
    GauntletStep(
        title = "Interrogation 6 of 12",
        question = "Mandatory typing test (1/2)",
        subtext = "Type the exact phrase below to prove your prefrontal cortex is still online:",
        waitSeconds = 8,
        confirmLabel = "Submit confession",
        abortLabel = "Cancel & keep protection",
        requiredPhrase = "i am choosing brainrot over my goals"
    ),
    GauntletStep(
        title = "Interrogation 7 of 12",
        question = "Are you REALLY, TRULY, GENUINELY sure?",
        subtext = "Notice how annoying this is? That's the entire point. You can still walk away.",
        waitSeconds = 15,
        confirmLabel = "Keep suffering through prompts",
        abortLabel = "Escape & keep blocker on",
        flipButtons = true
    ),
    GauntletStep(
        title = "Interrogation 8 of 12",
        question = "Imagine explaining this to yourself tomorrow morning.",
        subtext = "Tomorrow-You is watching Past-You wait out a countdown timer just to watch 15-second clips.",
        waitSeconds = 15,
        confirmLabel = "Tomorrow-Me can deal with it",
        abortLabel = "Protect Tomorrow-Me"
    ),
    GauntletStep(
        title = "Interrogation 9 of 12",
        question = "Wait... did someone send you a video in a chat?",
        subtext = "Reminder: Videos opened from DMs/Chats are ALREADY ALLOWED without turning this off!",
        waitSeconds = 12,
        confirmLabel = "No, I want the endless feed",
        abortLabel = "Oh nice, keep it on",
        flipButtons = true
    ),
    GauntletStep(
        title = "Interrogation 10 of 12",
        question = "Mandatory typing test (2/2)",
        subtext = "Type the exact phrase below (case-sensitive, no period):",
        waitSeconds = 10,
        confirmLabel = "Verify & continue",
        abortLabel = "I give up, keep me blocked",
        requiredPhrase = "my attention span is mine to waste"
    ),
    GauntletStep(
        title = "Interrogation 11 of 12",
        question = "Penultimate check: Are you beyond all reasonable doubt sure?",
        subtext = "One more screen after this. If you leave the app now, you start all over at Question 1.",
        waitSeconds = 18,
        confirmLabel = "Proceed to final lock",
        abortLabel = "Abort at the finish line",
        flipButtons = true
    ),
    GauntletStep(
        title = "Final Lock (12 of 12)",
        question = "Last chance. Unlock for 5 minutes?",
        subtext = "After 5 minutes, DoomscrollJail will automatically re-arm itself.",
        waitSeconds = 20,
        confirmLabel = "UNLOCK NOW",
        abortLabel = "STAY LOCKED IN"
    )
)

@Composable
fun GauntletScreen(
    actionDescription: String,
    onCompleted: () -> Unit,
    onCancelled: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val currentStep = DEFAULT_GAUNTLET_STEPS[stepIndex]

    var secondsRemaining by remember(stepIndex) { mutableIntStateOf(currentStep.waitSeconds) }
    var typedInput by remember(stepIndex) { mutableStateOf("") }
    var resetWarning by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                stepIndex = 0
                typedInput = ""
                resetWarning = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(stepIndex, resetWarning) {
        secondsRemaining = DEFAULT_GAUNTLET_STEPS[stepIndex].waitSeconds
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        }
    }

    val phraseMatches = currentStep.requiredPhrase == null ||
        typedInput.trim() == currentStep.requiredPhrase

    val canProceed = secondsRemaining == 0 && phraseMatches

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "THE DEACTIVATION GAUNTLET",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Requested: $actionDescription",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { (stepIndex + 1).toFloat() / DEFAULT_GAUNTLET_STEPS.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentStep.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (resetWarning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "You left the app! Progress reset to Question 1.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStep.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentStep.subtext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (currentStep.requiredPhrase != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = currentStep.requiredPhrase,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = typedInput,
                            onValueChange = { typedInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Type the exact phrase above") }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (secondsRemaining > 0) {
                        Text(
                            text = "Wait ${secondsRemaining}s before answering...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Timer complete. Choose wisely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val abortButton: @Composable () -> Unit = {
                    Button(
                        onClick = onCancelled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(
                            text = currentStep.abortLabel,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val proceedButton: @Composable () -> Unit = {
                    OutlinedButton(
                        onClick = {
                            resetWarning = false
                            if (stepIndex < DEFAULT_GAUNTLET_STEPS.lastIndex) {
                                stepIndex += 1
                            } else {
                                onCompleted()
                            }
                        },
                        enabled = canProceed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        val label = if (secondsRemaining > 0) {
                            "${currentStep.confirmLabel} (${secondsRemaining}s)"
                        } else {
                            currentStep.confirmLabel
                        }
                        Text(text = label)
                    }
                }

                if (currentStep.flipButtons) {
                    proceedButton()
                    abortButton()
                } else {
                    abortButton()
                    proceedButton()
                }
            }
        }
    }
}
