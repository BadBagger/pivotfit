package com.pivotfit.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pivotfit.app.data.db.ExerciseLogEntity
import com.pivotfit.app.data.db.WorkoutSessionEntity
import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.GeneratedWorkout
import com.pivotfit.app.data.models.RpeRating
import com.pivotfit.app.data.models.TodayCheckIn
import com.pivotfit.app.data.models.UserProfile
import com.pivotfit.app.data.models.WorkoutExercise
import com.pivotfit.app.data.repositories.PivotRepository
import com.pivotfit.app.domain.progression.ProgressionAdvisor
import com.pivotfit.app.domain.scoring.ConsistencyScorer
import com.pivotfit.app.domain.scoring.ProgressSummary
import com.pivotfit.app.domain.substitutions.PivotEngine
import com.pivotfit.app.domain.substitutions.PivotReason
import com.pivotfit.app.domain.workoutgenerator.AdaptiveWorkoutGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveExerciseState(
    val workoutExercise: WorkoutExercise,
    val done: Boolean = false,
    val rpe: RpeRating = RpeRating.Good,
    val pain: Boolean = false,
    val notes: String = ""
)

data class PivotUiState(
    val checkIn: TodayCheckIn = TodayCheckIn(),
    val generatedWorkout: GeneratedWorkout? = null,
    val activeExercises: List<ActiveExerciseState> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val pivotEvents: List<String> = emptyList(),
    val skippedExercises: List<String> = emptyList(),
    val workoutStartedAt: Long = System.currentTimeMillis(),
    val completionMessage: String = "Short workout is still a win."
)

class PivotViewModel(private val repository: PivotRepository) : ViewModel() {
    private val generator = AdaptiveWorkoutGenerator(repository.exercises)
    private val pivotEngine = PivotEngine(repository.exercises)
    private val scorer = ConsistencyScorer()
    private val progression = ProgressionAdvisor()

    private val internal = MutableStateFlow(PivotUiState())
    val uiState: StateFlow<PivotUiState> = internal

    val profile: StateFlow<UserProfile> = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())
    val sessions: StateFlow<List<WorkoutSessionEntity>> = repository.sessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val progress: StateFlow<ProgressSummary> = repository.sessions
        .combine(repository.userProfile) { sessions, _ -> scorer.summarize(sessions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), scorer.summarize(emptyList()))

    fun updateCheckIn(update: (TodayCheckIn) -> TodayCheckIn) {
        internal.value = internal.value.copy(checkIn = update(internal.value.checkIn))
    }

    fun saveProfile(profile: UserProfile) = viewModelScope.launch { repository.saveProfile(profile) }

    fun generateWorkout() {
        val workout = generator.generate(internal.value.checkIn, profile.value, sessions.value)
        internal.value = internal.value.copy(
            generatedWorkout = workout,
            activeExercises = workout.exercises.map { ActiveExerciseState(it) },
            currentExerciseIndex = 0,
            pivotEvents = emptyList(),
            skippedExercises = emptyList(),
            workoutStartedAt = System.currentTimeMillis()
        )
    }

    fun pivot(reason: PivotReason) {
        val state = internal.value
        val current = state.activeExercises.getOrNull(state.currentExerciseIndex) ?: return
        val replacement = pivotEngine.replacement(current.workoutExercise, reason, state.checkIn.equipmentAvailable)
        val exercises = state.activeExercises.toMutableList()
        exercises[state.currentExerciseIndex] = current.copy(workoutExercise = replacement)
        val event = "${reason.label}: ${current.workoutExercise.exercise.name} -> ${replacement.exercise.name}"
        internal.value = state.copy(activeExercises = exercises, pivotEvents = state.pivotEvents + event)
    }

    fun markCurrentDone(rpe: RpeRating = RpeRating.Good, pain: Boolean = false) {
        val state = internal.value
        val exercises = state.activeExercises.toMutableList()
        val current = exercises.getOrNull(state.currentExerciseIndex) ?: return
        exercises[state.currentExerciseIndex] = current.copy(done = true, rpe = rpe, pain = pain)
        internal.value = state.copy(activeExercises = exercises, currentExerciseIndex = (state.currentExerciseIndex + 1).coerceAtMost(exercises.lastIndex))
    }

    fun skipCurrent() {
        val state = internal.value
        val current = state.activeExercises.getOrNull(state.currentExerciseIndex) ?: return
        internal.value = state.copy(
            skippedExercises = state.skippedExercises + current.workoutExercise.exercise.name,
            currentExerciseIndex = (state.currentExerciseIndex + 1).coerceAtMost(state.activeExercises.lastIndex)
        )
    }

    fun setCurrentIndex(index: Int) {
        internal.value = internal.value.copy(currentExerciseIndex = index.coerceIn(0, internal.value.activeExercises.lastIndex.coerceAtLeast(0)))
    }

    fun completeWorkout(finalRpe: RpeRating, notes: String, ranOutOfTime: Boolean) = viewModelScope.launch {
        val state = internal.value
        val workout = state.generatedWorkout ?: return@launch
        val completed = state.activeExercises.count { it.done }
        val duration = ((System.currentTimeMillis() - state.workoutStartedAt) / 60_000).toInt().coerceAtLeast(1)
        val sessionId = UUID.randomUUID().toString()
        val session = WorkoutSessionEntity(
            id = sessionId,
            generatedWorkoutId = workout.id,
            title = workout.title,
            startedAt = state.workoutStartedAt,
            completedAt = System.currentTimeMillis(),
            durationMinutes = duration,
            exercisesCompleted = completed,
            rpe = finalRpe.label,
            notes = notes,
            pivotEvents = state.pivotEvents,
            skippedExercises = state.skippedExercises,
            sorenessFlags = state.checkIn.sorenessAreas.map { it.label },
            generatedWorkoutReason = workout.reason
        )
        val logs = state.activeExercises.map {
            ExerciseLogEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                exerciseId = it.workoutExercise.exercise.id,
                exerciseName = it.workoutExercise.exercise.name,
                setsRepsOrTime = it.workoutExercise.prescription,
                rpe = it.rpe.label,
                painFlag = it.pain,
                skipped = it.workoutExercise.exercise.name in state.skippedExercises,
                notes = it.notes
            )
        }
        repository.saveSession(session, logs)
        internal.value = state.copy(completionMessage = progression.nextStep(finalRpe, logs.any { it.painFlag }, ranOutOfTime))
    }

    fun deleteHistory() = viewModelScope.launch { repository.deleteHistory() }

    class Factory(private val repository: PivotRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PivotViewModel(repository) as T
    }
}
