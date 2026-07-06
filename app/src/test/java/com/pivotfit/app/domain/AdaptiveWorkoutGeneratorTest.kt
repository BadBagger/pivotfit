package com.pivotfit.app.domain

import com.pivotfit.app.data.db.WorkoutSessionEntity
import com.pivotfit.app.data.models.EnergyLevel
import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.ExerciseCategory
import com.pivotfit.app.data.models.FitnessGoal
import com.pivotfit.app.data.models.MuscleGroup
import com.pivotfit.app.data.models.TodayCheckIn
import com.pivotfit.app.data.models.UserProfile
import com.pivotfit.app.data.models.WorkoutLocation
import com.pivotfit.app.data.seed.ExerciseSeed
import com.pivotfit.app.domain.workoutgenerator.AdaptiveWorkoutGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveWorkoutGeneratorTest {
    private val generator = AdaptiveWorkoutGenerator(ExerciseSeed.exercises)

    @Test
    fun seedLibraryHasAtLeastEightyExercises() {
        assertTrue(ExerciseSeed.exercises.size >= 80)
    }

    @Test
    fun lowEnergyTenMinuteCheckInCreatesMinimumEffectiveWorkout() {
        val workout = generator.generate(
            checkIn = TodayCheckIn(
                timeAvailable = 10,
                energyLevel = EnergyLevel.Low,
                goalToday = FitnessGoal.MaintainStreak,
                equipmentAvailable = setOf(Equipment.Bodyweight)
            ),
            profile = UserProfile(beginnerMode = true),
            recentSessions = emptyList()
        )

        assertEquals("Minimum effective workout", workout.title)
        assertEquals(1, workout.exercises.size)
        assertTrue(workout.reason.contains("10 minutes"))
        assertTrue(workout.exercises.all { it.exercise.category == ExerciseCategory.Mobility })
    }

    @Test
    fun soreLegsAvoidsLegAndFullBodyMainExercises() {
        val workout = generator.generate(
            checkIn = TodayCheckIn(
                timeAvailable = 30,
                location = WorkoutLocation.Home,
                goalToday = FitnessGoal.Muscle,
                sorenessAreas = setOf(MuscleGroup.Legs),
                equipmentAvailable = setOf(Equipment.Bodyweight, Equipment.Dumbbells)
            ),
            profile = UserProfile(beginnerMode = true),
            recentSessions = emptyList()
        )

        assertTrue(workout.reason.contains("avoided legs", ignoreCase = true))
        assertFalse(workout.exercises.any { MuscleGroup.Legs in it.exercise.muscleGroups })
        assertFalse(workout.exercises.any { MuscleGroup.FullBody in it.exercise.muscleGroups })
    }

    @Test
    fun crowdedGymAvoidsBarbellAndBenchDependentExercises() {
        val workout = generator.generate(
            checkIn = TodayCheckIn(
                timeAvailable = 45,
                location = WorkoutLocation.Gym,
                goalToday = FitnessGoal.Muscle,
                crowdedGym = true,
                equipmentAvailable = setOf(Equipment.Dumbbells, Equipment.Bench, Equipment.Barbell, Equipment.Machines)
            ),
            profile = UserProfile(beginnerMode = false),
            recentSessions = emptyList()
        )

        assertFalse(workout.exercises.any { Equipment.Barbell in it.exercise.equipment })
        assertFalse(workout.exercises.any { Equipment.Bench in it.exercise.equipment })
    }

    @Test
    fun workBreakLowSweatModeChoosesQuietGentleCardio() {
        val workout = generator.generate(
            checkIn = TodayCheckIn(
                timeAvailable = 10,
                location = WorkoutLocation.WorkBreak,
                goalToday = FitnessGoal.Cardio,
                energyLevel = EnergyLevel.Medium,
                lowSweatMode = true,
                quietMode = true,
                equipmentAvailable = setOf(Equipment.Bodyweight)
            ),
            profile = UserProfile(beginnerMode = true),
            recentSessions = emptyList()
        )

        assertEquals("Work break reset", workout.title)
        assertTrue(workout.exercises.all { it.exercise.quietFriendly })
        assertTrue(workout.exercises.all { it.exercise.id in setOf("easy-walk", "brisk-walk", "march-in-place") })
    }

    @Test
    fun missedThreeDaysCreatesComebackWithRestartFraming() {
        val fourDaysAgo = System.currentTimeMillis() - 4L * 86_400_000L
        val workout = generator.generate(
            checkIn = TodayCheckIn(
                timeAvailable = 15,
                goalToday = FitnessGoal.GeneralHealth,
                equipmentAvailable = setOf(Equipment.Bodyweight)
            ),
            profile = UserProfile(beginnerMode = true),
            recentSessions = listOf(session(startedAt = fourDaysAgo))
        )

        assertEquals("15-minute comeback", workout.title)
        assertTrue(workout.reason.contains("restart workout"))
        assertTrue(workout.reason.contains("not a punishment", ignoreCase = true))
    }

    private fun session(startedAt: Long): WorkoutSessionEntity =
        WorkoutSessionEntity(
            id = "session",
            generatedWorkoutId = "generated",
            title = "Previous workout",
            startedAt = startedAt,
            completedAt = startedAt + 20 * 60_000,
            durationMinutes = 20,
            exercisesCompleted = 3,
            rpe = "Good",
            notes = "",
            pivotEvents = emptyList(),
            skippedExercises = emptyList(),
            sorenessFlags = emptyList(),
            generatedWorkoutReason = ""
        )
}
