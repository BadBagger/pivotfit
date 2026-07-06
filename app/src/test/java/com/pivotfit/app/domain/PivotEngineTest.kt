package com.pivotfit.app.domain

import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.WorkoutExercise
import com.pivotfit.app.data.seed.ExerciseSeed
import com.pivotfit.app.domain.substitutions.PivotEngine
import com.pivotfit.app.domain.substitutions.PivotReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PivotEngineTest {
    private val engine = PivotEngine(ExerciseSeed.exercises)

    @Test
    fun equipmentUnavailableReplacesCurrentExerciseWithAvailableEquipment() {
        val current = WorkoutExercise(
            exercise = ExerciseSeed.exercises.first { it.id == "barbell-bench" },
            prescription = "3 sets of 8-10 reps",
            restSeconds = 60,
            note = ""
        )

        val replacement = engine.replacement(
            current = current,
            reason = PivotReason.EquipmentUnavailable,
            available = setOf(Equipment.Dumbbells, Equipment.Bodyweight)
        )

        assertNotEquals("barbell-bench", replacement.exercise.id)
        assertTrue(replacement.exercise.equipment.any { it in setOf(Equipment.Dumbbells, Equipment.Bodyweight) })
        assertEquals(current.prescription, replacement.prescription)
        assertTrue(replacement.note.contains("Pivoted"))
    }

    @Test
    fun outOfTimeShortensPrescription() {
        val current = WorkoutExercise(
            exercise = ExerciseSeed.exercises.first { it.id == "pushup" },
            prescription = "3 sets of 8-10 reps",
            restSeconds = 60,
            note = ""
        )

        val replacement = engine.replacement(current, PivotReason.OutOfTime, setOf(Equipment.Bodyweight))

        assertEquals("1 quick set or 90 sec, then move on", replacement.prescription)
    }
}
