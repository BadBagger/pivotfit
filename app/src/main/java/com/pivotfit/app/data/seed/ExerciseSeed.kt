package com.pivotfit.app.data.seed

import com.pivotfit.app.data.models.Difficulty
import com.pivotfit.app.data.models.Equipment
import com.pivotfit.app.data.models.Exercise
import com.pivotfit.app.data.models.ExerciseCategory
import com.pivotfit.app.data.models.MovementPattern
import com.pivotfit.app.data.models.MuscleGroup

object ExerciseSeed {
    val exercises: List<Exercise> = buildList {
        fun addExercise(
            id: String,
            name: String,
            category: ExerciseCategory,
            groups: Set<MuscleGroup>,
            equipment: Set<Equipment>,
            difficulty: Difficulty,
            pattern: MovementPattern,
            quiet: Boolean = true,
            apartment: Boolean = true,
            gym: Boolean = true,
            home: Boolean = true
        ) {
            add(
                Exercise(
                    id = id,
                    name = name,
                    category = category,
                    muscleGroups = groups,
                    equipment = equipment,
                    difficulty = difficulty,
                    movementPattern = pattern,
                    instructions = "Move with control, keep a steady breath, and stop if sharp pain shows up.",
                    commonMistakes = "Rushing reps, holding breath, or forcing range of motion.",
                    quietFriendly = quiet,
                    apartmentFriendly = apartment,
                    gym = gym,
                    home = home
                )
            )
        }

        val bw = setOf(Equipment.Bodyweight)
        val db = setOf(Equipment.Dumbbells)
        val band = setOf(Equipment.ResistanceBands)
        val bb = setOf(Equipment.Barbell)
        val kb = setOf(Equipment.Kettlebell)
        val machine = setOf(Equipment.Machines)
        val benchDb = setOf(Equipment.Dumbbells, Equipment.Bench)

        listOf(
            Triple("incline-pushup", "Incline push-up", MuscleGroup.Chest),
            Triple("pushup", "Push-up", MuscleGroup.Chest),
            Triple("knee-pushup", "Knee push-up", MuscleGroup.Chest),
            Triple("wall-pushup", "Wall push-up", MuscleGroup.Chest),
            Triple("pike-pushup", "Pike push-up", MuscleGroup.Shoulders),
            Triple("chair-dip", "Chair triceps dip", MuscleGroup.Arms),
            Triple("bodyweight-squat", "Bodyweight squat", MuscleGroup.Legs),
            Triple("box-squat", "Box squat", MuscleGroup.Legs),
            Triple("reverse-lunge", "Reverse lunge", MuscleGroup.Legs),
            Triple("split-squat", "Split squat", MuscleGroup.Legs),
            Triple("glute-bridge", "Glute bridge", MuscleGroup.Legs),
            Triple("calf-raise", "Calf raise", MuscleGroup.Legs),
            Triple("plank", "Forearm plank", MuscleGroup.Core),
            Triple("side-plank", "Side plank", MuscleGroup.Core),
            Triple("dead-bug", "Dead bug", MuscleGroup.Core),
            Triple("bird-dog", "Bird dog", MuscleGroup.Core),
            Triple("mountain-climber", "Mountain climber", MuscleGroup.Core),
            Triple("bear-crawl-hold", "Bear crawl hold", MuscleGroup.FullBody)
        ).forEach { (id, name, group) ->
            addExercise(id, name, ExerciseCategory.Strength, setOf(group), bw, Difficulty.Easy, when (group) {
                MuscleGroup.Chest -> MovementPattern.Push
                MuscleGroup.Legs -> MovementPattern.Squat
                MuscleGroup.Core -> MovementPattern.Core
                else -> MovementPattern.Push
            }, quiet = id != "mountain-climber")
        }

        listOf(
            Triple("db-floor-press", "Dumbbell floor press", MuscleGroup.Chest),
            Triple("db-bench-press", "Dumbbell bench press", MuscleGroup.Chest),
            Triple("db-shoulder-press", "Dumbbell shoulder press", MuscleGroup.Shoulders),
            Triple("db-lateral-raise", "Dumbbell lateral raise", MuscleGroup.Shoulders),
            Triple("db-row", "One-arm dumbbell row", MuscleGroup.Back),
            Triple("db-reverse-fly", "Dumbbell reverse fly", MuscleGroup.Back),
            Triple("db-goblet-squat", "Dumbbell goblet squat", MuscleGroup.Legs),
            Triple("db-romanian-deadlift", "Dumbbell Romanian deadlift", MuscleGroup.Legs),
            Triple("db-step-up", "Dumbbell step-up", MuscleGroup.Legs),
            Triple("db-curl", "Dumbbell curl", MuscleGroup.Arms),
            Triple("db-triceps-extension", "Dumbbell triceps extension", MuscleGroup.Arms),
            Triple("db-farmer-carry", "Dumbbell farmer carry", MuscleGroup.FullBody),
            Triple("db-suitcase-carry", "Dumbbell suitcase carry", MuscleGroup.Core),
            Triple("db-thruster", "Dumbbell thruster", MuscleGroup.FullBody)
        ).forEach { (id, name, group) ->
            addExercise(id, name, ExerciseCategory.Strength, setOf(group), if (id.contains("bench")) benchDb else db, Difficulty.Moderate, when (group) {
                MuscleGroup.Chest, MuscleGroup.Shoulders, MuscleGroup.Arms -> MovementPattern.Push
                MuscleGroup.Back -> MovementPattern.Pull
                MuscleGroup.Core -> MovementPattern.Core
                MuscleGroup.FullBody -> MovementPattern.Carry
                else -> MovementPattern.Hinge
            })
        }

        listOf(
            Triple("barbell-squat", "Barbell back squat", MuscleGroup.Legs),
            Triple("barbell-deadlift", "Barbell deadlift", MuscleGroup.Legs),
            Triple("barbell-bench", "Barbell bench press", MuscleGroup.Chest),
            Triple("barbell-row", "Barbell row", MuscleGroup.Back),
            Triple("barbell-overhead-press", "Barbell overhead press", MuscleGroup.Shoulders),
            Triple("barbell-hip-thrust", "Barbell hip thrust", MuscleGroup.Legs),
            Triple("barbell-curl", "Barbell curl", MuscleGroup.Arms),
            Triple("landmine-press", "Landmine press", MuscleGroup.Shoulders)
        ).forEach { (id, name, group) ->
            addExercise(id, name, ExerciseCategory.Strength, setOf(group), bb, Difficulty.Hard, when (group) {
                MuscleGroup.Back -> MovementPattern.Pull
                MuscleGroup.Chest, MuscleGroup.Shoulders, MuscleGroup.Arms -> MovementPattern.Push
                else -> MovementPattern.Hinge
            }, apartment = false, home = false)
        }

        listOf(
            Triple("machine-chest-press", "Machine chest press", MuscleGroup.Chest),
            Triple("machine-row", "Seated machine row", MuscleGroup.Back),
            Triple("lat-pulldown", "Lat pulldown", MuscleGroup.Back),
            Triple("leg-press", "Leg press", MuscleGroup.Legs),
            Triple("leg-curl", "Leg curl", MuscleGroup.Legs),
            Triple("leg-extension", "Leg extension", MuscleGroup.Legs),
            Triple("cable-face-pull", "Cable face pull", MuscleGroup.Shoulders),
            Triple("cable-pressdown", "Cable triceps pressdown", MuscleGroup.Arms),
            Triple("cable-curl", "Cable curl", MuscleGroup.Arms),
            Triple("pec-deck", "Pec deck", MuscleGroup.Chest)
        ).forEach { (id, name, group) ->
            addExercise(id, name, ExerciseCategory.Strength, setOf(group), machine, Difficulty.Moderate, if (group == MuscleGroup.Back) MovementPattern.Pull else MovementPattern.Push, apartment = false, home = false)
        }

        listOf(
            Triple("band-row", "Resistance band row", MuscleGroup.Back),
            Triple("band-chest-press", "Resistance band chest press", MuscleGroup.Chest),
            Triple("band-pull-apart", "Band pull-apart", MuscleGroup.Shoulders),
            Triple("band-good-morning", "Band good morning", MuscleGroup.Legs),
            Triple("band-squat", "Band squat", MuscleGroup.Legs),
            Triple("band-lateral-walk", "Band lateral walk", MuscleGroup.Legs),
            Triple("band-curl", "Band curl", MuscleGroup.Arms),
            Triple("band-pressdown", "Band pressdown", MuscleGroup.Arms),
            Triple("band-pallof-press", "Band Pallof press", MuscleGroup.Core),
            Triple("band-dead-bug-pulldown", "Band dead bug pulldown", MuscleGroup.Core)
        ).forEach { (id, name, group) ->
            addExercise(id, name, ExerciseCategory.Strength, setOf(group), band, Difficulty.Easy, if (group == MuscleGroup.Back) MovementPattern.Pull else MovementPattern.Core)
        }

        listOf(
            Triple("kb-deadlift", "Kettlebell deadlift", MuscleGroup.Legs),
            Triple("kb-swing", "Kettlebell swing", MuscleGroup.FullBody),
            Triple("kb-goblet-squat", "Kettlebell goblet squat", MuscleGroup.Legs),
            Triple("kb-clean", "Kettlebell clean", MuscleGroup.FullBody),
            Triple("kb-press", "Kettlebell press", MuscleGroup.Shoulders),
            Triple("kb-halo", "Kettlebell halo", MuscleGroup.Shoulders)
        ).forEach { (id, name, group) ->
            addExercise(id, name, ExerciseCategory.Strength, setOf(group), kb, Difficulty.Moderate, MovementPattern.Hinge, quiet = id != "kb-swing")
        }

        listOf(
            "brisk-walk" to "Brisk walk",
            "easy-walk" to "Easy walk",
            "treadmill-walk" to "Treadmill walk",
            "treadmill-intervals" to "Treadmill intervals",
            "bike-easy" to "Easy bike",
            "bike-intervals" to "Bike intervals",
            "rower-easy" to "Easy row",
            "rower-intervals" to "Rower intervals",
            "stairs-easy" to "Easy stair climb",
            "march-in-place" to "March in place",
            "step-jacks" to "Low-impact step jacks",
            "shadow-boxing" to "Low-impact shadow boxing"
        ).forEach { (id, name) ->
            val equipment = when {
                id.contains("treadmill") -> setOf(Equipment.Treadmill)
                id.contains("bike") -> setOf(Equipment.Bike)
                id.contains("rower") -> setOf(Equipment.Rower)
                else -> bw
            }
            addExercise(id, name, ExerciseCategory.Cardio, setOf(MuscleGroup.FullBody), equipment, Difficulty.Easy, MovementPattern.Cardio, quiet = !id.contains("jacks"))
        }

        listOf(
            "cat-cow" to MuscleGroup.Back,
            "world-greatest-stretch" to MuscleGroup.FullBody,
            "hip-flexor-stretch" to MuscleGroup.Legs,
            "hamstring-sweep" to MuscleGroup.Legs,
            "ankle-rocker" to MuscleGroup.Legs,
            "thoracic-rotation" to MuscleGroup.Back,
            "shoulder-cars" to MuscleGroup.Shoulders,
            "wrist-mobility" to MuscleGroup.Arms,
            "childs-pose-breathing" to MuscleGroup.Back,
            "doorway-chest-stretch" to MuscleGroup.Chest,
            "neck-gentle-reset" to MuscleGroup.Shoulders,
            "box-breathing" to MuscleGroup.FullBody,
            "couch-stretch" to MuscleGroup.Legs,
            "figure-four-stretch" to MuscleGroup.Legs,
            "calf-wall-stretch" to MuscleGroup.Legs,
            "open-book" to MuscleGroup.Back
        ).forEach { (id, group) ->
            addExercise(
                id = id,
                name = id.split("-").joinToString(" ") { it.replaceFirstChar(Char::uppercase) },
                category = if (id.contains("breathing")) ExerciseCategory.Recovery else ExerciseCategory.Mobility,
                groups = setOf(group),
                equipment = bw,
                difficulty = Difficulty.Easy,
                pattern = MovementPattern.Mobility
            )
        }
    }
}
