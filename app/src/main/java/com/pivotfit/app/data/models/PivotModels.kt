package com.pivotfit.app.data.models

enum class FitnessGoal(val label: String) {
    Strength("Strength"),
    Muscle("Build muscle"),
    FatLoss("Lose fat"),
    Endurance("Endurance"),
    GeneralHealth("General health"),
    Mobility("Mobility"),
    Cardio("Cardio"),
    MaintainStreak("Maintain streak"),
    Recovery("Recovery")
}

enum class ExperienceLevel(val label: String) { Beginner("Beginner"), Returning("Returning"), Intermediate("Intermediate") }
enum class EnergyLevel(val label: String) { Low("Low"), Medium("Medium"), High("High") }
enum class WorkoutLocation(val label: String) { Home("Home"), Gym("Gym"), Hotel("Hotel"), Outside("Outside"), WorkBreak("Work break") }
enum class ExerciseCategory(val label: String) { Strength("Strength"), Cardio("Cardio"), Mobility("Mobility"), Recovery("Recovery") }
enum class Difficulty(val label: String) { Easy("Easy"), Moderate("Moderate"), Hard("Hard") }
enum class MovementPattern(val label: String) { Push("Push"), Pull("Pull"), Squat("Squat"), Hinge("Hinge"), Lunge("Lunge"), Carry("Carry"), Core("Core"), Cardio("Cardio"), Mobility("Mobility") }
enum class MuscleGroup(val label: String) { Legs("Legs"), Chest("Chest"), Back("Back"), Shoulders("Shoulders"), Arms("Arms"), Core("Core"), FullBody("Full body") }
enum class Equipment(val label: String) { Bodyweight("Bodyweight"), Dumbbells("Dumbbells"), ResistanceBands("Resistance bands"), Barbell("Barbell"), Kettlebell("Kettlebell"), Machines("Machines"), PullUpBar("Pull-up bar"), Bench("Bench"), Treadmill("Treadmill"), Bike("Bike"), Rower("Rower") }
enum class RpeRating(val label: String) { Easy("Easy"), Good("Good"), Hard("Hard"), TooHard("Too hard") }

data class UserProfile(
    val goal: FitnessGoal = FitnessGoal.GeneralHealth,
    val experienceLevel: ExperienceLevel = ExperienceLevel.Beginner,
    val preferredWorkoutLength: Int = 20,
    val availableEquipment: Set<Equipment> = setOf(Equipment.Bodyweight),
    val preferredDays: Set<String> = emptySet(),
    val limitations: Set<MuscleGroup> = emptySet(),
    val beginnerMode: Boolean = true,
    val quietWorkoutPreference: Boolean = false,
    val lowSweatPreference: Boolean = false,
    val flexiblePlan: Boolean = true
)

data class TodayCheckIn(
    val timeAvailable: Int = 20,
    val location: WorkoutLocation = WorkoutLocation.Home,
    val energyLevel: EnergyLevel = EnergyLevel.Medium,
    val sorenessAreas: Set<MuscleGroup> = emptySet(),
    val goalToday: FitnessGoal = FitnessGoal.GeneralHealth,
    val equipmentAvailable: Set<Equipment> = setOf(Equipment.Bodyweight),
    val crowdedGym: Boolean = false,
    val quietMode: Boolean = false,
    val lowSweatMode: Boolean = false
)

data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val muscleGroups: Set<MuscleGroup>,
    val equipment: Set<Equipment>,
    val difficulty: Difficulty,
    val movementPattern: MovementPattern,
    val instructions: String,
    val commonMistakes: String,
    val easierAlternativeIds: List<String> = emptyList(),
    val harderAlternativeIds: List<String> = emptyList(),
    val equipmentAlternativeIds: List<String> = emptyList(),
    val quietFriendly: Boolean = true,
    val apartmentFriendly: Boolean = true,
    val gym: Boolean = true,
    val home: Boolean = true
)

data class WorkoutExercise(
    val exercise: Exercise,
    val prescription: String,
    val restSeconds: Int,
    val note: String
)

data class GeneratedWorkout(
    val id: String,
    val title: String,
    val goal: FitnessGoal,
    val estimatedDuration: Int,
    val warmup: List<WorkoutExercise>,
    val exercises: List<WorkoutExercise>,
    val cooldown: List<WorkoutExercise>,
    val reason: String,
    val difficulty: Difficulty,
    val createdAt: Long = System.currentTimeMillis()
)

data class PivotEvent(
    val reason: String,
    val originalExercise: String,
    val replacementExercise: String,
    val timeSaved: Int,
    val timestamp: Long = System.currentTimeMillis()
)
