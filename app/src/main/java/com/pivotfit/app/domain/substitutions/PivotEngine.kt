package com.pivotfit.app.domain.substitutions

import com.pivotfit.app.data.models.Difficulty
import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.Exercise
import com.pivotfit.app.data.models.WorkoutExercise

enum class PivotReason(val label: String) {
    TooHard("Exercise too hard"),
    EquipmentUnavailable("Equipment unavailable"),
    Pain("Pain/discomfort"),
    Crowded("Too crowded"),
    OutOfTime("Running out of time"),
    TooEasy("Too easy"),
    Quiet("Need quiet apartment version")
}

class PivotEngine(private val library: List<Exercise>) {
    fun replacement(current: WorkoutExercise, reason: PivotReason, available: Set<Equipment>): WorkoutExercise {
        val exercise = current.exercise
        val match = library.firstOrNull {
            it.id != exercise.id &&
                it.category == exercise.category &&
                it.movementPattern == exercise.movementPattern &&
                it.equipment.any { eq -> eq in available } &&
                when (reason) {
                    PivotReason.TooHard, PivotReason.Pain -> it.difficulty != Difficulty.Hard
                    PivotReason.Quiet -> it.quietFriendly && it.apartmentFriendly
                    PivotReason.EquipmentUnavailable, PivotReason.Crowded -> it.equipment != exercise.equipment
                    PivotReason.TooEasy -> it.difficulty != Difficulty.Easy
                    PivotReason.OutOfTime -> true
                }
        } ?: library.firstOrNull { it.category == exercise.category && it.equipment.any { eq -> eq in available } } ?: exercise

        val prescription = when (reason) {
            PivotReason.OutOfTime -> "1 quick set or 90 sec, then move on"
            PivotReason.TooEasy -> "Add 2-4 reps or one careful extra set"
            PivotReason.TooHard, PivotReason.Pain -> "Reduce range and keep it easy; stop if discomfort continues"
            else -> current.prescription
        }
        return current.copy(exercise = match, prescription = prescription, note = "Pivoted: ${reason.label}. This keeps the workout alive.")
    }
}
