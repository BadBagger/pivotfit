package com.pivotfit.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PivotTableChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pivotfit.app.data.models.Difficulty
import com.pivotfit.app.data.models.EnergyLevel
import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.Exercise
import com.pivotfit.app.data.models.ExerciseCategory
import com.pivotfit.app.data.models.ExperienceLevel
import com.pivotfit.app.data.models.FitnessGoal
import com.pivotfit.app.data.models.MuscleGroup
import com.pivotfit.app.data.models.MovementPattern
import com.pivotfit.app.data.models.RpeRating
import com.pivotfit.app.data.models.TodayCheckIn
import com.pivotfit.app.data.models.UserProfile
import com.pivotfit.app.data.models.WorkoutLocation
import com.pivotfit.app.data.repositories.PivotRepository
import com.pivotfit.app.domain.substitutions.PivotReason
import com.pivotfit.app.R
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
private enum class Screen(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    CheckIn("Check in", Icons.Default.FitnessCenter),
    Active("Workout", Icons.Default.PivotTableChart),
    Progress("Progress", Icons.Default.Check),
    Settings("Settings", Icons.Default.Settings)
}

private enum class SecondaryScreen {
    Builder, Complete, Plan, Library, Equipment, Preferences, Recovery, History, Privacy, Safety, Detail, Onboarding
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PivotFitRoot(repository: PivotRepository) {
    val vm: PivotViewModel = viewModel(factory = PivotViewModel.Factory(repository))
    val state by vm.uiState.collectAsState()
    val profile by vm.profile.collectAsState()
    val onboardingComplete by vm.onboardingComplete.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val progress by vm.progress.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.Home.name) }
    var secondary by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            if (onboardingComplete && secondary != SecondaryScreen.Onboarding.name) {
                NavigationBar {
                    Screen.entries.forEach { item ->
                        NavigationBarItem(
                            selected = screen == item.name && secondary == null,
                            onClick = { screen = item.name; secondary = null },
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            if (!onboardingComplete && secondary == null) {
                OnboardingScreen(profile = profile, onComplete = { saved ->
                    vm.completeOnboarding(saved)
                    screen = Screen.Home.name
                    secondary = null
                })
            } else when (secondary?.let { SecondaryScreen.valueOf(it) }) {
                SecondaryScreen.Builder -> WorkoutBuilderScreen(state, onStart = { screen = Screen.Active.name; secondary = null }, onBack = { secondary = null })
                SecondaryScreen.Complete -> WorkoutCompleteScreen(state, onSave = vm::completeWorkout, onHome = { screen = Screen.Home.name; secondary = null })
                SecondaryScreen.Plan -> PlanScreen(profile, onSave = vm::saveProfile)
                SecondaryScreen.Library -> ExerciseLibraryScreen(repository.exercises, onDetail = { selectedExerciseId = it.id; secondary = SecondaryScreen.Detail.name })
                SecondaryScreen.Equipment -> EquipmentScreen(state.checkIn, onUpdate = vm::updateCheckIn)
                SecondaryScreen.Preferences -> PreferencesScreen(profile, onSave = vm::saveProfile)
                SecondaryScreen.Recovery -> RecoveryAndSorenessScreen(state.checkIn, onUpdate = vm::updateCheckIn)
                SecondaryScreen.History -> HistoryScreen(sessions)
                SecondaryScreen.Privacy -> PrivacyScreen(onDeleteHistory = vm::deleteHistory)
                SecondaryScreen.Safety -> SafetyScreen()
                SecondaryScreen.Detail -> ExerciseDetailScreen(repository.exercises.firstOrNull { it.id == selectedExerciseId }, onBack = { secondary = SecondaryScreen.Library.name })
                SecondaryScreen.Onboarding -> OnboardingScreen(profile = profile, onComplete = { saved ->
                    vm.completeOnboarding(saved)
                    screen = Screen.Home.name
                    secondary = null
                })
                null -> when (Screen.valueOf(screen)) {
                    Screen.Home -> HomeScreen(
                        progress = progress,
                        sessions = sessions,
                        onCheckIn = { screen = Screen.CheckIn.name },
                        onPlan = { secondary = SecondaryScreen.Plan.name },
                        onLibrary = { secondary = SecondaryScreen.Library.name },
                        onRecovery = { secondary = SecondaryScreen.Recovery.name },
                        onHistory = { secondary = SecondaryScreen.History.name }
                    )
                    Screen.CheckIn -> TodayCheckInScreen(
                        checkIn = state.checkIn,
                        onUpdate = vm::updateCheckIn,
                        onBuild = { vm.generateWorkout(); secondary = SecondaryScreen.Builder.name },
                        onEquipment = { secondary = SecondaryScreen.Equipment.name }
                    )
                    Screen.Active -> ActiveWorkoutScreen(
                        state = state,
                        onDone = vm::markCurrentDone,
                        onSkip = vm::skipCurrent,
                        onPivot = vm::pivot,
                        onSelect = vm::setCurrentIndex,
                        onComplete = { secondary = SecondaryScreen.Complete.name }
                    )
                    Screen.Progress -> ProgressScreen(progress, sessions, onHistory = { secondary = SecondaryScreen.History.name })
                    Screen.Settings -> SettingsScreen(
                        onPreferences = { secondary = SecondaryScreen.Preferences.name },
                        onPlan = { secondary = SecondaryScreen.Plan.name },
                        onPrivacy = { secondary = SecondaryScreen.Privacy.name },
                        onSafety = { secondary = SecondaryScreen.Safety.name },
                        onOnboarding = { secondary = SecondaryScreen.Onboarding.name }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingScreen(profile: UserProfile, onComplete: (UserProfile) -> Unit) {
    var draft by remember(profile) { mutableStateOf(profile) }
    ScreenList {
        Text("Set your default plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MessageCard("Real-life defaults", "PivotFit will use these as a starting point, then adapt each workout to the day you actually have.")
        EnumChips("Main goal", FitnessGoal.entries.filter { it in listOf(FitnessGoal.Strength, FitnessGoal.Muscle, FitnessGoal.FatLoss, FitnessGoal.Endurance, FitnessGoal.GeneralHealth, FitnessGoal.Mobility) }, draft.goal, { it.label }) {
            draft = draft.copy(goal = it)
        }
        EnumChips("Experience", ExperienceLevel.entries, draft.experienceLevel, { it.label }) {
            draft = draft.copy(experienceLevel = it, beginnerMode = it == ExperienceLevel.Beginner)
        }
        ChipGroup("Preferred length", listOf(10, 20, 30, 45, 60), draft.preferredWorkoutLength, { "${it}m" }) {
            draft = draft.copy(preferredWorkoutLength = it)
        }
        MultiEnumChips("Equipment you usually have", Equipment.entries, draft.availableEquipment, { it.label }) { selected ->
            draft = draft.copy(availableEquipment = selected.ifEmpty { setOf(Equipment.Bodyweight) })
        }
        RowToggle("Beginner mode", draft.beginnerMode) { draft = draft.copy(beginnerMode = !draft.beginnerMode) }
        RowToggle("Prefer quiet workouts", draft.quietWorkoutPreference) { draft = draft.copy(quietWorkoutPreference = !draft.quietWorkoutPreference) }
        RowToggle("Prefer low-sweat options", draft.lowSweatPreference) { draft = draft.copy(lowSweatPreference = !draft.lowSweatPreference) }
        RowToggle("Flexible weekly plan", draft.flexiblePlan) { draft = draft.copy(flexiblePlan = !draft.flexiblePlan) }
        BigButton("Start using PivotFit") { onComplete(draft) }
    }
}

@Composable
private fun HomeScreen(
    progress: com.pivotfit.app.domain.scoring.ProgressSummary,
    sessions: List<com.pivotfit.app.data.db.WorkoutSessionEntity>,
    onCheckIn: () -> Unit,
    onPlan: () -> Unit,
    onLibrary: () -> Unit,
    onRecovery: () -> Unit,
    onHistory: () -> Unit
) {
    ScreenList {
        Text("PivotFit", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text("Train around real life.", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge)
        BigButton("Build today's workout", onCheckIn)
        if (sessions.isEmpty()) MessageCard("Start simple", "Tell PivotFit what today actually looks like. A short workout still counts.")
        else MessageCard("Current momentum", "${progress.currentMomentum}. ${progress.workouts} workouts, ${progress.minutes} minutes logged.")
        Text("No problem. Want a restart workout?", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRecovery, modifier = Modifier.weight(1f)) { Text("5-min reset") }
            OutlinedButton(onClick = onCheckIn, modifier = Modifier.weight(1f)) { Text("Comeback") }
        }
        QuickLink("Flexible plan builder", "Set targets like 2 strength, 1 cardio, 1 recovery session.", onPlan)
        QuickLink("Exercise library", "Browse the local library and substitutions.", onLibrary)
        QuickLink("History", "Review completed workouts and pivots.", onHistory)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodayCheckInScreen(checkIn: TodayCheckIn, onUpdate: ((TodayCheckIn) -> TodayCheckIn) -> Unit, onBuild: () -> Unit, onEquipment: () -> Unit) {
    ScreenList {
        Text("What can you realistically do today?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        TimeAvailableSlider(checkIn.timeAvailable) { time -> onUpdate { it.copy(timeAvailable = time) } }
        EnumChips("Location", WorkoutLocation.entries, checkIn.location, { it.label }) { value -> onUpdate { it.copy(location = value) } }
        EnumChips("Energy", EnergyLevel.entries, checkIn.energyLevel, { it.label }) { value -> onUpdate { it.copy(energyLevel = value) } }
        MultiEnumChips("Sore areas", MuscleGroup.entries, checkIn.sorenessAreas, { it.label }) { value -> onUpdate { it.copy(sorenessAreas = value) } }
        EnumChips("Goal today", FitnessGoal.entries.filter { it in listOf(FitnessGoal.Muscle, FitnessGoal.FatLoss, FitnessGoal.Mobility, FitnessGoal.Cardio, FitnessGoal.MaintainStreak, FitnessGoal.Recovery, FitnessGoal.Strength) }, checkIn.goalToday, { it.label }) { value -> onUpdate { it.copy(goalToday = value) } }
        RowToggle("Gym is crowded", checkIn.crowdedGym) { onUpdate { it.copy(crowdedGym = !it.crowdedGym) } }
        RowToggle("Quiet apartment mode", checkIn.quietMode) { onUpdate { it.copy(quietMode = !it.quietMode) } }
        RowToggle("Low-sweat work break", checkIn.lowSweatMode) { onUpdate { it.copy(lowSweatMode = !it.lowSweatMode) } }
        QuickLink("Equipment available", checkIn.equipmentAvailable.joinToString { it.label }, onEquipment)
        BigButton("Create workout", onBuild)
    }
}

@Composable
private fun TimeAvailableSlider(minutes: Int, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            SectionTitle("Time")
            Text("$minutes min", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Slider(
                    value = minutes.toFloat().coerceIn(5f, 60f),
                    onValueChange = { onChange(it.roundToInt().coerceIn(5, 60)) },
                    valueRange = 5f..60f
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("5 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("60 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun WorkoutBuilderScreen(state: PivotUiState, onStart: () -> Unit, onBack: () -> Unit) {
    val workout = state.generatedWorkout
    ScreenList {
        if (workout == null) {
            MessageCard("No workout yet", "Run today's check-in first.")
            BigButton("Back to check-in", onBack)
        } else {
            Text(workout.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            MessageCard("Why this workout", workout.reason)
            SectionTitle("Warmup")
            workout.warmup.forEach { ExerciseRow(it.exercise.name, it.prescription, it.note) }
            SectionTitle("Main")
            workout.exercises.forEach { ExerciseRow(it.exercise.name, it.prescription, "Rest ${it.restSeconds}s. ${it.note}") }
            SectionTitle("Cooldown")
            workout.cooldown.forEach { ExerciseRow(it.exercise.name, it.prescription, it.note) }
            BigButton("Start workout", onStart)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveWorkoutScreen(
    state: PivotUiState,
    onDone: (RpeRating, Boolean) -> Unit,
    onSkip: () -> Unit,
    onPivot: (PivotReason) -> Unit,
    onSelect: (Int) -> Unit,
    onComplete: () -> Unit
) {
    var pivotOpen by remember { mutableStateOf(false) }
    var pain by remember { mutableStateOf(false) }
    var rpe by remember { mutableStateOf(RpeRating.Good) }
    val current = state.activeExercises.getOrNull(state.currentExerciseIndex)
    var setNumber by rememberSaveable { mutableStateOf(1) }
    var restRemaining by rememberSaveable { mutableStateOf(0) }
    val totalSets = current?.workoutExercise?.prescription?.let(::setCountFromPrescription) ?: 1
    val isResting = restRemaining > 0

    LaunchedEffect(state.currentExerciseIndex, current?.workoutExercise?.exercise?.id) {
        setNumber = 1
        restRemaining = 0
        pain = false
        rpe = RpeRating.Good
    }
    LaunchedEffect(restRemaining) {
        if (restRemaining > 0) {
            delay(1_000)
            restRemaining -= 1
        }
    }

    ScreenList {
        if (current == null) {
            MessageCard("Ready when you are", "Build a workout from today's check-in.")
            return@ScreenList
        }
        ActiveWorkoutHeader(state)
        ActiveExerciseHero(
            exercise = current.workoutExercise.exercise,
            prescription = current.workoutExercise.prescription,
            setNumber = setNumber,
            totalSets = totalSets,
            restSeconds = current.workoutExercise.restSeconds
        )
        if (isResting) {
            RestTimerCard(
                remainingSeconds = restRemaining,
                onSkipRest = { restRemaining = 0 }
            )
        }
        EnumChips("How did it feel?", RpeRating.entries, rpe, { it.label }) { rpe = it }
        RowToggle("Pain or discomfort", pain) { pain = !pain }
        Button(
            enabled = !isResting && !current.done,
            onClick = {
                if (setNumber < totalSets) {
                    setNumber += 1
                    restRemaining = current.workoutExercise.restSeconds
                } else {
                    onDone(rpe, pain)
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                when {
                    current.done -> "Exercise complete"
                    setNumber < totalSets -> "Finish set"
                    else -> "Finish exercise"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Skip")
            }
            Button(
                onClick = { pivotOpen = true },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("Pivot") }
        }
        if (state.activeExercises.all { it.done } || state.currentExerciseIndex == state.activeExercises.lastIndex) {
            OutlinedButton(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(8.dp)) { Text("Finish workout") }
        }
        ExerciseGuidanceCard(current.workoutExercise.exercise)
        SectionTitle("Workout list")
        state.activeExercises.forEachIndexed { index, item ->
            AssistChip(
                onClick = { onSelect(index) },
                label = { Text("${if (item.done) "Done" else "${index + 1}."} ${item.workoutExercise.exercise.name}") },
                leadingIcon = { if (item.done) Icon(Icons.Default.Check, null) }
            )
        }
    }
    if (pivotOpen) {
        AlertDialog(
            onDismissRequest = { pivotOpen = false },
            title = { Text("Want to pivot?") },
            text = {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PivotReason.entries.forEach { reason ->
                        AssistChip(onClick = { onPivot(reason); pivotOpen = false }, label = { Text(reason.label) })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pivotOpen = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun ActiveWorkoutHeader(state: PivotUiState) {
    val completed = state.activeExercises.count { it.done }
    val total = state.activeExercises.size.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Active workout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("$completed of $total exercises complete", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${((completed.toFloat() / total) * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { completed.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}

@Composable
private fun ActiveExerciseHero(
    exercise: Exercise,
    prescription: String,
    setNumber: Int,
    totalSets: Int,
    restSeconds: Int
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ZoomableExerciseImage(exercise = exercise, heightDp = 220)
            Text(exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("Set", "$setNumber/$totalSets", Modifier.weight(1f))
                MiniStat("Target", prescription, Modifier.weight(1.35f))
                MiniStat("Rest", "${restSeconds}s", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RestTimerCard(remainingSeconds: Int, onSkipRest: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Rest", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                Text(formatSeconds(remainingSeconds), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
            OutlinedButton(onClick = onSkipRest, shape = RoundedCornerShape(8.dp)) { Text("Skip rest") }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = modifier) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(value, fontWeight = FontWeight.Bold, maxLines = 2)
        }
    }
}

private fun setCountFromPrescription(prescription: String): Int =
    Regex("""(\d+)\s+sets?""", RegexOption.IGNORE_CASE)
        .find(prescription)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(1, 8)
        ?: 1

private fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remainder = seconds % 60
    return "$minutes:${remainder.toString().padStart(2, '0')}"
}

@Composable
private fun ExerciseGuidanceCard(exercise: Exercise) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("How to do this", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            ZoomableExerciseImage(exercise = exercise, heightDp = 240)
            exerciseSteps(exercise).forEachIndexed { index, step ->
                Text("${index + 1}. $step", color = MaterialTheme.colorScheme.onSurface)
            }
            Text("Watch for: ${exerciseMistakes(exercise)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Stop if you feel sharp pain. Pivot to an easier option if this does not feel right.", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ZoomableExerciseImage(exercise: Exercise, heightDp: Int) {
    var expanded by remember { mutableStateOf(false) }
    val imageRes = exerciseGuidanceImageRes(exercise)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "${exercise.name} form example",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            "View larger",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxSize().padding(12.dp)
            ) {
                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = "${exercise.name} enlarged form example",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    OutlinedButton(onClick = { expanded = false }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

private fun exerciseGuidanceImageRes(exercise: Exercise): Int =
    when (exercise.id) {
        "bear-crawl-hold" -> R.drawable.exercise_bear_crawl
        "bodyweight-squat" -> R.drawable.exercise_squat
        "reverse-lunge", "split-squat", "db-step-up" -> R.drawable.exercise_lunge
        "glute-bridge", "barbell-hip-thrust" -> R.drawable.exercise_glute_bridge
        "calf-raise" -> R.drawable.exercise_calf_raise
        "plank" -> R.drawable.exercise_plank
        "side-plank" -> R.drawable.exercise_side_plank
        "dead-bug", "band-dead-bug-pulldown" -> R.drawable.exercise_dead_bug
        "bird-dog" -> R.drawable.exercise_bird_dog
        "mountain-climber" -> R.drawable.exercise_mountain_climber
        "db-curl", "barbell-curl", "cable-curl", "band-curl" -> R.drawable.exercise_curl
        "chair-dip", "db-triceps-extension", "cable-pressdown", "band-pressdown" -> R.drawable.exercise_triceps_extension
        "pike-pushup", "db-shoulder-press", "barbell-overhead-press", "landmine-press", "kb-press" -> R.drawable.exercise_shoulder_press
        "leg-press" -> R.drawable.exercise_leg_press
        "leg-extension" -> R.drawable.exercise_leg_extension
        "leg-curl" -> R.drawable.exercise_leg_curl
        "kb-swing" -> R.drawable.exercise_kettlebell_swing
        "kb-clean" -> R.drawable.exercise_kettlebell_clean
        "kb-halo" -> R.drawable.exercise_kettlebell_halo
        "brisk-walk", "easy-walk", "treadmill-walk", "treadmill-intervals", "stairs-easy" -> R.drawable.exercise_walk
        "bike-easy", "bike-intervals" -> R.drawable.exercise_bike
        "rower-easy", "rower-intervals" -> R.drawable.exercise_rower
        "shadow-boxing" -> R.drawable.exercise_shadow_boxing
        "cat-cow", "childs-pose-breathing" -> R.drawable.exercise_floor_mobility
        else -> when (exercise.movementPattern) {
            MovementPattern.Push -> R.drawable.exercise_push
            MovementPattern.Pull -> R.drawable.exercise_pull
            MovementPattern.Squat -> R.drawable.exercise_squat
            MovementPattern.Hinge -> R.drawable.exercise_hinge
            MovementPattern.Lunge -> R.drawable.exercise_lunge
            MovementPattern.Carry -> R.drawable.exercise_carry
            MovementPattern.Core -> R.drawable.exercise_bear_crawl
            MovementPattern.Cardio -> R.drawable.exercise_cardio
            MovementPattern.Mobility -> R.drawable.exercise_mobility
        }
    }

private fun exerciseSteps(exercise: Exercise): List<String> =
    when (exercise.id) {
        "bear-crawl-hold" -> listOf(
            "Start on hands and knees with hands under shoulders and knees under hips.",
            "Brace your stomach, then lift knees one or two inches off the floor.",
            "Keep your back flat and hips quiet. Hold while breathing slowly."
        )
        "db-reverse-fly" -> listOf(
            "Hinge forward with a long back and soft knees.",
            "Let the dumbbells hang under your shoulders, palms facing each other.",
            "Raise arms out wide until your upper back works, then lower with control."
        )
        else -> when (exercise.movementPattern) {
            MovementPattern.Push -> listOf("Set hands or handles steady before you start.", "Brace your core and lower with control.", "Press away smoothly without shrugging your shoulders.")
            MovementPattern.Pull -> listOf("Start tall with shoulders down, not shrugged.", "Pull elbows toward your ribs or hips.", "Pause briefly, then return slowly without yanking.")
            MovementPattern.Squat -> listOf("Stand with feet planted and ribs stacked over hips.", "Sit down and back as knees track over toes.", "Push through the floor to stand tall without bouncing.")
            MovementPattern.Hinge -> listOf("Soften knees and push hips back.", "Keep your back long and weight close to your body.", "Squeeze glutes to stand tall without leaning back.")
            MovementPattern.Lunge -> listOf("Start tall and step into a stable stance.", "Lower until both legs feel controlled.", "Drive through the front foot and keep your torso steady.")
            MovementPattern.Carry -> listOf("Stand tall with weight held firmly.", "Keep ribs down, shoulders level, and steps quiet.", "Walk slowly or hold position without leaning.")
            MovementPattern.Core -> listOf("Brace your midsection before moving.", "Keep ribs and hips from twisting or sagging.", "Use slow breaths and stop before form breaks.")
            MovementPattern.Cardio -> listOf("Start easier than you think you need.", "Keep breathing steady and posture relaxed.", "Increase pace only if it still feels controlled.")
            MovementPattern.Mobility -> listOf("Move into a gentle range, not a painful stretch.", "Breathe slowly and keep the motion smooth.", "Back off if your body guards or pinches.")
        }
    }

private fun exerciseMistakes(exercise: Exercise): String =
    when (exercise.movementPattern) {
        MovementPattern.Push -> "shrugging, flaring elbows hard, or rushing the lower."
        MovementPattern.Pull -> "jerking the weight, rounding forward, or pulling with the neck."
        MovementPattern.Squat -> "knees collapsing in, heels lifting, or dropping faster than you can control."
        MovementPattern.Hinge -> "rounding your back, squatting instead of hinging, or letting weight drift away."
        MovementPattern.Lunge -> "wobbling through reps, front knee caving in, or pushing through pain."
        MovementPattern.Carry -> "leaning to one side, holding your breath, or rushing steps."
        MovementPattern.Core -> "hips sagging, breath holding, or turning the hold into a strain."
        MovementPattern.Cardio -> "starting too hard, stomping, or ignoring discomfort."
        MovementPattern.Mobility -> "forcing range, bouncing, or stretching through sharp pain."
    }

@Composable
private fun WorkoutCompleteScreen(state: PivotUiState, onSave: (RpeRating, String, Boolean) -> Unit, onHome: () -> Unit) {
    var rpe by remember { mutableStateOf(RpeRating.Good) }
    var notes by remember { mutableStateOf("") }
    var ranOut by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    ScreenList {
        Text("Workout complete", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (!saved) {
            CompletionPreview(state)
            EnumChips("Overall", RpeRating.entries, rpe, { it.label }) { rpe = it }
            RowToggle("Ran out of time", ranOut) { ranOut = !ranOut }
            TextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            BigButton("Save workout") { onSave(rpe, notes, ranOut); saved = true }
        } else {
            CompletionSavedSummary(state.lastCompletionSummary)
        }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Home") }
    }
}

@Composable
private fun CompletionPreview(state: PivotUiState) {
    val completed = state.activeExercises.count { it.done }
    val total = state.activeExercises.size
    MessageCard("Good work", "Short workout is still a win. $completed of $total exercises completed.")
    SummaryGrid(
        items = listOf(
            "Exercises" to "$completed/$total",
            "Pivots" to state.pivotEvents.size.toString(),
            "Skipped" to state.skippedExercises.size.toString(),
            "Soreness flags" to state.checkIn.sorenessAreas.size.toString()
        )
    )
    if (state.pivotEvents.isNotEmpty()) {
        MessageCard("Pivots used", state.pivotEvents.joinToString("\n"))
    }
    if (state.skippedExercises.isNotEmpty()) {
        MessageCard("Skipped", state.skippedExercises.joinToString("\n"))
    }
}

@Composable
private fun CompletionSavedSummary(summary: WorkoutCompletionSummary?) {
    if (summary == null) {
        MessageCard("Saved", "Workout saved. Short workout is still a win.")
        return
    }
    MessageCard("Saved", "${summary.title}\n${summary.durationMinutes} min, ${summary.completedExercises}/${summary.totalExercises} exercises, RPE ${summary.overallRpe}")
    SummaryGrid(
        items = listOf(
            "Minutes" to summary.durationMinutes.toString(),
            "Exercises" to "${summary.completedExercises}/${summary.totalExercises}",
            "Pivots" to summary.pivotsUsed.toString(),
            "Skipped" to summary.skippedExercises.size.toString()
        )
    )
    if (summary.sorenessFlags.isNotEmpty()) {
        MessageCard("Soreness noted", summary.sorenessFlags.joinToString())
    }
    if (summary.skippedExercises.isNotEmpty()) {
        MessageCard("Skipped exercises", summary.skippedExercises.joinToString("\n"))
    }
    MessageCard("Next adjustment", summary.nextRecommendation)
}

@Composable
private fun ProgressScreen(progress: com.pivotfit.app.domain.scoring.ProgressSummary, sessions: List<com.pivotfit.app.data.db.WorkoutSessionEntity>, onHistory: () -> Unit) {
    ScreenList {
        Text("Progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MessageCard("Consistency Score", "${progress.consistencyScore}/100 - ${progress.currentMomentum}")
        StatRow("Workouts completed", progress.workouts.toString())
        StatRow("Minutes trained", progress.minutes.toString())
        StatRow("Comeback wins", progress.comebackWins.toString())
        StatRow("Suggested next focus", if (sessions.any { it.sorenessFlags.isNotEmpty() }) "Recovery balance" else "Keep the weekly mix flexible")
        QuickLink("View history", "See completed workouts, pivots, and notes.", onHistory)
    }
}

@Composable
private fun PlanScreen(profile: UserProfile, onSave: (UserProfile) -> Unit) {
    var draft by remember(profile) { mutableStateOf(profile) }
    ScreenList {
        Text("Flexible plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Default target: 2 strength sessions, 1 cardio session, 1 mobility or recovery session.")
        EnumChips("Main goal", FitnessGoal.entries.take(6), draft.goal, { it.label }) { draft = draft.copy(goal = it) }
        EnumChips("Experience", ExperienceLevel.entries, draft.experienceLevel, { it.label }) { draft = draft.copy(experienceLevel = it) }
        ChipGroup("Preferred length", listOf(10, 20, 30, 45, 60), draft.preferredWorkoutLength, { "${it}m" }) { draft = draft.copy(preferredWorkoutLength = it) }
        RowToggle("Flexible plan", draft.flexiblePlan) { draft = draft.copy(flexiblePlan = !draft.flexiblePlan) }
        RowToggle("Beginner mode", draft.beginnerMode) { draft = draft.copy(beginnerMode = !draft.beginnerMode) }
        BigButton("Save plan") { onSave(draft) }
    }
}

@Composable
private fun ExerciseLibraryScreen(exercises: List<Exercise>, onDetail: (Exercise) -> Unit) {
    var filter by remember { mutableStateOf(ExerciseCategory.Strength) }
    ScreenList {
        Text("Exercise library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        EnumChips("Category", ExerciseCategory.entries, filter, { it.label }) { filter = it }
        exercises.filter { it.category == filter }.forEach {
            QuickLink(it.name, "${it.movementPattern.label} - ${it.equipment.joinToString { eq -> eq.label }}", { onDetail(it) })
        }
    }
}

@Composable
private fun ExerciseDetailScreen(exercise: Exercise?, onBack: () -> Unit) {
    ScreenList {
        if (exercise == null) {
            MessageCard("Missing exercise", "Pick another library item.")
        } else {
            Text(exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            StatRow("Category", exercise.category.label)
            StatRow("Difficulty", exercise.difficulty.label)
            StatRow("Muscles", exercise.muscleGroups.joinToString { it.label })
            StatRow("Equipment", exercise.equipment.joinToString { it.label })
            ExerciseGuidanceCard(exercise)
            MessageCard("Alternatives", "Use the Pivot button if this is too hard, too easy, unavailable, or uncomfortable.")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun EquipmentScreen(checkIn: TodayCheckIn, onUpdate: ((TodayCheckIn) -> TodayCheckIn) -> Unit) {
    ScreenList {
        Text("Equipment", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MultiEnumChips("Available today", Equipment.entries, checkIn.equipmentAvailable, { it.label }) { selected -> onUpdate { it.copy(equipmentAvailable = selected.ifEmpty { setOf(Equipment.Bodyweight) }) } }
    }
}

@Composable
private fun PreferencesScreen(profile: UserProfile, onSave: (UserProfile) -> Unit) {
    var draft by remember(profile) { mutableStateOf(profile) }
    ScreenList {
        Text("Preferences", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        RowToggle("Beginner mode", draft.beginnerMode) { draft = draft.copy(beginnerMode = !draft.beginnerMode) }
        RowToggle("Quiet workouts", draft.quietWorkoutPreference) { draft = draft.copy(quietWorkoutPreference = !draft.quietWorkoutPreference) }
        RowToggle("Low-sweat preference", draft.lowSweatPreference) { draft = draft.copy(lowSweatPreference = !draft.lowSweatPreference) }
        BigButton("Save preferences") { onSave(draft) }
    }
}

@Composable
private fun RecoveryAndSorenessScreen(checkIn: TodayCheckIn, onUpdate: ((TodayCheckIn) -> TodayCheckIn) -> Unit) {
    ScreenList {
        Text("Recovery", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MessageCard("Safety note", "May help you move easier. Skip anything that causes pain. For injuries, follow professional medical advice.")
        MultiEnumChips("Sore areas", MuscleGroup.entries, checkIn.sorenessAreas, { it.label }) { value -> onUpdate { it.copy(sorenessAreas = value, goalToday = FitnessGoal.Recovery) } }
        MessageCard("Recovery ideas", "Mobility, easy walk, low-intensity cardio, breathing cooldown, and gentle sore-area relief.")
    }
}

@Composable
private fun HistoryScreen(sessions: List<com.pivotfit.app.data.db.WorkoutSessionEntity>) {
    ScreenList {
        Text("History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (sessions.isEmpty()) MessageCard("No history yet", "Completed workouts will stay on this device.")
        sessions.forEach {
            MessageCard(
                it.title,
                "${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it.startedAt))}\n${it.durationMinutes} min, ${it.exercisesCompleted} exercises, RPE ${it.rpe}\n${it.generatedWorkoutReason}"
            )
        }
    }
}

@Composable
private fun SettingsScreen(onPreferences: () -> Unit, onPlan: () -> Unit, onPrivacy: () -> Unit, onSafety: () -> Unit, onOnboarding: () -> Unit) {
    ScreenList {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        QuickLink("Run onboarding", "Update goal, experience, equipment, and default workout preferences.", onOnboarding)
        QuickLink("Preferences", "Beginner mode, quiet workouts, low-sweat defaults.", onPreferences)
        QuickLink("Plan builder", "Flexible weekly targets and experience level.", onPlan)
        QuickLink("Privacy", "Local-first data controls.", onPrivacy)
        QuickLink("Safety", "General fitness and pain guidance.", onSafety)
        MessageCard("Notifications placeholder", "Supportive reminders are planned: configurable, no spam, no guilt, no fake urgency.")
        MessageCard("Premium-ready flags", "Future features can include advanced analytics, exports, custom exercises, Health Connect, and cloud backup. Billing is not implemented.")
    }
}

@Composable
private fun PrivacyScreen(onDeleteHistory: () -> Unit) {
    ScreenList {
        Text("Privacy", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MessageCard("Local-first", "No account required. Workout history stays on this device. No ads. No selling fitness data. Health Connect can be optional later.")
        OutlinedButton(onClick = onDeleteHistory, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Delete local history") }
    }
}

@Composable
private fun SafetyScreen() {
    ScreenList {
        Text("Safety", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MessageCard("General fitness only", "Stop if you feel sharp pain. PivotFit does not diagnose or treat medical conditions.")
        MessageCard("When to get help", "Consult a professional for injuries, medical conditions, pregnancy, rehab, or anything that feels unsafe.")
    }
}

@Composable
private fun ScreenList(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun MessageCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickLink(title: String, body: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BigButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ExerciseRow(name: String, prescription: String, note: String) {
    MessageCard(name, "$prescription\n$note")
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SummaryGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (label, value) ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RowToggle(label: String, value: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Medium)
        Switch(checked = value, onCheckedChange = { onToggle() })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(title: String, values: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                FilterChip(selected = value == selected, onClick = { onSelect(value) }, label = { Text(label(value)) })
            }
        }
    }
}

@Composable
private fun <T> EnumChips(title: String, values: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) =
    ChipGroup(title, values, selected, label, onSelect)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T> MultiEnumChips(title: String, values: List<T>, selected: Set<T>, label: (T) -> String, onSelect: (Set<T>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                FilterChip(
                    selected = value in selected,
                    onClick = { onSelect(if (value in selected) selected - value else selected + value) },
                    label = { Text(label(value)) }
                )
            }
        }
    }
}
