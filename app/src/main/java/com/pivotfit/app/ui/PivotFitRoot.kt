package com.pivotfit.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pivotfit.app.data.models.Difficulty
import com.pivotfit.app.data.models.EnergyLevel
import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.Exercise
import com.pivotfit.app.data.models.ExerciseCategory
import com.pivotfit.app.data.models.ExperienceLevel
import com.pivotfit.app.data.models.FitnessGoal
import com.pivotfit.app.data.models.MuscleGroup
import com.pivotfit.app.data.models.RpeRating
import com.pivotfit.app.data.models.TodayCheckIn
import com.pivotfit.app.data.models.UserProfile
import com.pivotfit.app.data.models.WorkoutLocation
import com.pivotfit.app.data.repositories.PivotRepository
import com.pivotfit.app.domain.substitutions.PivotReason
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
private enum class Screen(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    CheckIn("Check in", Icons.Default.FitnessCenter),
    Active("Workout", Icons.Default.PivotTableChart),
    Progress("Progress", Icons.Default.Check),
    Settings("Settings", Icons.Default.Settings)
}

private enum class SecondaryScreen {
    Builder, Complete, Plan, Library, Equipment, Preferences, Recovery, History, Privacy, Safety, Detail
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PivotFitRoot(repository: PivotRepository) {
    val vm: PivotViewModel = viewModel(factory = PivotViewModel.Factory(repository))
    val state by vm.uiState.collectAsState()
    val profile by vm.profile.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val progress by vm.progress.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.Home.name) }
    var secondary by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
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
    ) { padding ->
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            when (secondary?.let { SecondaryScreen.valueOf(it) }) {
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
                        onSafety = { secondary = SecondaryScreen.Safety.name }
                    )
                }
            }
        }
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
        ChipGroup("Time", listOf(5, 10, 20, 30, 45, 60), checkIn.timeAvailable, { it.toString() }) { time -> onUpdate { it.copy(timeAvailable = time) } }
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
    ScreenList {
        if (current == null) {
            MessageCard("Ready when you are", "Build a workout from today's check-in.")
            return@ScreenList
        }
        Text("Active workout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = { state.activeExercises.count { it.done }.toFloat() / state.activeExercises.size.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary)
                Text(current.workoutExercise.exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(current.workoutExercise.prescription, style = MaterialTheme.typography.titleLarge)
                Text("Rest ${current.workoutExercise.restSeconds}s after the set.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        EnumChips("How did it feel?", RpeRating.entries, rpe, { it.label }) { rpe = it }
        RowToggle("Pain or discomfort", pain) { pain = !pain }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onDone(rpe, pain); pain = false; rpe = RpeRating.Good }, modifier = Modifier.weight(1f).height(58.dp)) {
                Text("Done")
            }
            OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f).height(58.dp)) { Text("Skip") }
        }
        Button(
            onClick = { pivotOpen = true },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { Text("Pivot") }
        if (state.activeExercises.all { it.done } || state.currentExerciseIndex == state.activeExercises.lastIndex) {
            OutlinedButton(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Finish workout") }
        }
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
private fun WorkoutCompleteScreen(state: PivotUiState, onSave: (RpeRating, String, Boolean) -> Unit, onHome: () -> Unit) {
    var rpe by remember { mutableStateOf(RpeRating.Good) }
    var notes by remember { mutableStateOf("") }
    var ranOut by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    ScreenList {
        Text("Workout complete", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        MessageCard("Good work", "Short workout is still a win. ${state.activeExercises.count { it.done }} exercises completed.")
        EnumChips("Overall", RpeRating.entries, rpe, { it.label }) { rpe = it }
        RowToggle("Ran out of time", ranOut) { ranOut = !ranOut }
        TextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        BigButton(if (saved) "Saved" else "Save workout") { onSave(rpe, notes, ranOut); saved = true }
        MessageCard("Next adjustment", state.completionMessage)
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Home") }
    }
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
            MessageCard("Instructions", exercise.instructions)
            MessageCard("Common mistakes", exercise.commonMistakes)
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
private fun SettingsScreen(onPreferences: () -> Unit, onPlan: () -> Unit, onPrivacy: () -> Unit, onSafety: () -> Unit) {
    ScreenList {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
