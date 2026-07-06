package com.pivotfit.app.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val generatedWorkoutId: String,
    val title: String,
    val startedAt: Long,
    val completedAt: Long?,
    val durationMinutes: Int,
    val exercisesCompleted: Int,
    val rpe: String,
    val notes: String,
    val pivotEvents: List<String>,
    val skippedExercises: List<String>,
    val sorenessFlags: List<String>,
    val generatedWorkoutReason: String
)

@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val setsRepsOrTime: String,
    val rpe: String,
    val painFlag: Boolean,
    val skipped: Boolean,
    val notes: String
)

class PivotConverters {
    @TypeConverter fun fromList(value: List<String>): String = value.joinToString("|")
    @TypeConverter fun toList(value: String): List<String> = value.split("|").filter { it.isNotBlank() }
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    suspend fun sessions(): List<WorkoutSessionEntity>

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert
    suspend fun insertExerciseLogs(logs: List<ExerciseLogEntity>)

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteSessions()

    @Query("DELETE FROM exercise_logs")
    suspend fun deleteExerciseLogs()
}

@Database(entities = [WorkoutSessionEntity::class, ExerciseLogEntity::class], version = 1, exportSchema = false)
@TypeConverters(PivotConverters::class)
abstract class PivotFitDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
