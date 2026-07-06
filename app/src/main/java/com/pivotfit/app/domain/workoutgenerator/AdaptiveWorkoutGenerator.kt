package com.pivotfit.app.domain.workoutgenerator

import com.pivotfit.app.data.db.WorkoutSessionEntity
import com.pivotfit.app.data.models.Difficulty
import com.pivotfit.app.data.models.EnergyLevel
import com.pivotfit.app.data.models.Exercise
import com.pivotfit.app.data.models.ExerciseCategory
import com.pivotfit.app.data.models.FitnessGoal
import com.pivotfit.app.data.models.GeneratedWorkout
import com.pivotfit.app.data.models.MovementPattern
import com.pivotfit.app.data.models.MuscleGroup
import com.pivotfit.app.data.models.TodayCheckIn
import com.pivotfit.app.data.models.UserProfile
import com.pivotfit.app.data.models.WorkoutExercise
import com.pivotfit.app.data.models.WorkoutLocation
import kotlin.math.max

class AdaptiveWorkoutGenerator(private val library: List<Exercise>) {
    fun generate(
        checkIn: TodayCheckIn,
        profile: UserProfile,
        recentSessions: List<WorkoutSessionEntity>
    ): GeneratedWorkout {
        val missedDays = daysSinceLastWorkout(recentSessions)
        val workoutSize = when {
            checkIn.timeAvailable <= 5 -> 1
            checkIn.timeAvailable <= 10 -> 2
            checkIn.timeAvailable <= 20 -> 3
            checkIn.timeAvailable <= 30 -> 4
            else -> 5
        }.let { if (checkIn.energyLevel == EnergyLevel.Low) max(1, it - 1) else it }

        val modeCategory = when (checkIn.goalToday) {
            FitnessGoal.Mobility -> ExerciseCategory.Mobility
            FitnessGoal.Cardio, FitnessGoal.FatLoss -> ExerciseCategory.Cardio
            FitnessGoal.Recovery -> ExerciseCategory.Recovery
            FitnessGoal.MaintainStreak -> if (checkIn.energyLevel == EnergyLevel.Low) ExerciseCategory.Mobility else ExerciseCategory.Strength
            else -> ExerciseCategory.Strength
        }

        val candidates = library
            .asSequence()
            .filter { modeCategory == it.category || (modeCategory == ExerciseCategory.Recovery && it.category == ExerciseCategory.Mobility) }
            .filter { exercise -> exercise.equipment.any { it in checkIn.equipmentAvailable } || exercise.equipment.isEmpty() }
            .filter { exercise -> checkIn.sorenessAreas.none { it in exercise.muscleGroups || it == MuscleGroup.FullBody } }
            .filter { exercise -> checkIn.location != WorkoutLocation.WorkBreak || exercise.quietFriendly }
            .filter { exercise -> !checkIn.quietMode || exercise.quietFriendly }
            .filter { exercise -> !checkIn.crowdedGym || exercise.equipment.none { it.name in setOf("Barbell", "Bench") } }
            .filter { exercise -> when (checkIn.location) {
                WorkoutLocation.Gym -> exercise.gym
                WorkoutLocation.Home, WorkoutLocation.Hotel, WorkoutLocation.WorkBreak -> exercise.home || exercise.apartmentFriendly
                WorkoutLocation.Outside -> exercise.category == ExerciseCategory.Cardio || exercise.equipment.all { it.name == "Bodyweight" }
            } }
            .sortedWith(compareBy<Exercise> { if (profile.beginnerMode && it.difficulty == Difficulty.Hard) 1 else 0 }.thenBy { it.name })
            .toList()

        val fallback = library.filter { it.category == ExerciseCategory.Mobility || it.category == ExerciseCategory.Recovery }
        val selected = balancePatterns(if (candidates.isEmpty()) fallback else candidates, workoutSize)

        val warmup = library.filter { it.category == ExerciseCategory.Mobility }.take(2).map {
            WorkoutExercise(it, "30-45 sec", 15, "Easy movement prep")
        }
        val cooldown = library.filter { it.category == ExerciseCategory.Recovery || it.id.contains("breathing") }.take(1).map {
            WorkoutExercise(it, "60 sec", 0, "Downshift before you leave")
        }
        val main = selected.map {
            WorkoutExercise(
                exercise = it,
                prescription = prescription(checkIn, it),
                restSeconds = if (checkIn.timeAvailable <= 10) 20 else if (checkIn.energyLevel == EnergyLevel.Low) 45 else 60,
                note = "Easier: slow the pace or reduce reps. Harder: add a round, reps, or load."
            )
        }

        val title = when {
            missedDays >= 3 -> "${checkIn.timeAvailable}-minute comeback"
            checkIn.energyLevel == EnergyLevel.Low -> "Minimum effective workout"
            checkIn.goalToday == FitnessGoal.Recovery -> "Recovery reset"
            checkIn.location == WorkoutLocation.WorkBreak -> "Work break reset"
            else -> "Today's real-life plan"
        }
        val reason = buildReason(checkIn, missedDays, selected)
        return GeneratedWorkout(
            id = "generated-${System.currentTimeMillis()}",
            title = title,
            goal = checkIn.goalToday,
            estimatedDuration = checkIn.timeAvailable,
            warmup = warmup,
            exercises = main,
            cooldown = cooldown,
            reason = reason,
            difficulty = if (checkIn.energyLevel == EnergyLevel.High && !profile.beginnerMode) Difficulty.Hard else Difficulty.Moderate
        )
    }

    private fun balancePatterns(candidates: List<Exercise>, target: Int): List<Exercise> {
        val result = mutableListOf<Exercise>()
        val preferredPatterns = listOf(MovementPattern.Push, MovementPattern.Pull, MovementPattern.Squat, MovementPattern.Hinge, MovementPattern.Core, MovementPattern.Cardio, MovementPattern.Mobility)
        preferredPatterns.forEach { pattern ->
            if (result.size < target) candidates.firstOrNull { it.movementPattern == pattern && it !in result }?.let(result::add)
        }
        candidates.forEach { if (result.size < target && it !in result) result.add(it) }
        return result
    }

    private fun prescription(checkIn: TodayCheckIn, exercise: Exercise): String = when (exercise.category) {
        ExerciseCategory.Cardio -> if (checkIn.timeAvailable <= 10) "4 rounds: 40 sec easy, 20 sec steady" else "8-20 min steady, conversational pace"
        ExerciseCategory.Mobility, ExerciseCategory.Recovery -> "45-60 sec, gentle range"
        ExerciseCategory.Strength -> when {
            checkIn.timeAvailable <= 10 -> "1-2 sets of 6-10 reps"
            checkIn.energyLevel == EnergyLevel.Low -> "2 sets of 6-8 easy reps"
            checkIn.energyLevel == EnergyLevel.High -> "3-4 sets of 8-12 reps"
            else -> "3 sets of 8-10 reps"
        }
    }

    private fun buildReason(checkIn: TodayCheckIn, missedDays: Int, selected: List<Exercise>): String {
        val soreness = if (checkIn.sorenessAreas.isEmpty()) "No soreness flags were set." else "We avoided ${checkIn.sorenessAreas.joinToString { it.label.lowercase() }}."
        val comeback = if (missedDays >= 3) "No problem: this is a restart workout, not a punishment. " else ""
        val equipment = selected.flatMap { it.equipment }.distinct().joinToString { it.label }
        return comeback + "Built for ${checkIn.timeAvailable} minutes, ${checkIn.energyLevel.label.lowercase()} energy, and ${checkIn.location.label.lowercase()}. $soreness Equipment used: $equipment."
    }

    private fun daysSinceLastWorkout(sessions: List<WorkoutSessionEntity>): Int {
        val last = sessions.maxByOrNull { it.startedAt }?.startedAt ?: return 0
        val dayMs = 86_400_000L
        return ((System.currentTimeMillis() - last) / dayMs).toInt()
    }
}
