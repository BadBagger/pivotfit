package com.pivotfit.app.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.pivotfit.app.data.db.ExerciseLogEntity
import com.pivotfit.app.data.db.PivotFitDatabase
import com.pivotfit.app.data.db.WorkoutSessionEntity
import com.pivotfit.app.data.models.Exercise
import com.pivotfit.app.data.models.ExperienceLevel
import com.pivotfit.app.data.models.FitnessGoal
import com.pivotfit.app.data.models.UserProfile
import com.pivotfit.app.data.seed.ExerciseSeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pivotDataStore by preferencesDataStore("pivotfit_preferences")

class PivotRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = Room.databaseBuilder(appContext, PivotFitDatabase::class.java, "pivotfit.db").build()
    private val prefs = appContext.pivotDataStore

    val exercises: List<Exercise> = ExerciseSeed.exercises
    val sessions: Flow<List<WorkoutSessionEntity>> = db.workoutDao().observeSessions()

    val userProfile: Flow<UserProfile> = prefs.data.map { preferences ->
        UserProfile(
            goal = FitnessGoal.valueOf(preferences[Keys.goal] ?: FitnessGoal.GeneralHealth.name),
            experienceLevel = ExperienceLevel.valueOf(preferences[Keys.experience] ?: ExperienceLevel.Beginner.name),
            preferredWorkoutLength = preferences[Keys.length]?.toIntOrNull() ?: 20,
            beginnerMode = preferences[Keys.beginner] ?: true,
            quietWorkoutPreference = preferences[Keys.quiet] ?: false,
            lowSweatPreference = preferences[Keys.lowSweat] ?: false,
            flexiblePlan = preferences[Keys.flexible] ?: true
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        prefs.edit {
            it[Keys.goal] = profile.goal.name
            it[Keys.experience] = profile.experienceLevel.name
            it[Keys.length] = profile.preferredWorkoutLength.toString()
            it[Keys.beginner] = profile.beginnerMode
            it[Keys.quiet] = profile.quietWorkoutPreference
            it[Keys.lowSweat] = profile.lowSweatPreference
            it[Keys.flexible] = profile.flexiblePlan
        }
    }

    suspend fun saveSession(session: WorkoutSessionEntity, logs: List<ExerciseLogEntity>) {
        db.workoutDao().insertSession(session)
        db.workoutDao().insertExerciseLogs(logs)
    }

    suspend fun deleteHistory() {
        db.workoutDao().deleteExerciseLogs()
        db.workoutDao().deleteSessions()
    }

    private object Keys {
        val goal = androidx.datastore.preferences.core.stringPreferencesKey("goal")
        val experience = androidx.datastore.preferences.core.stringPreferencesKey("experience")
        val length = androidx.datastore.preferences.core.stringPreferencesKey("length")
        val beginner = booleanPreferencesKey("beginner")
        val quiet = booleanPreferencesKey("quiet")
        val lowSweat = booleanPreferencesKey("low_sweat")
        val flexible = booleanPreferencesKey("flexible")
    }
}
