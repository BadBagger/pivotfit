package com.pivotfit.app.domain.scoring

import com.pivotfit.app.data.db.WorkoutSessionEntity

data class ProgressSummary(
    val workouts: Int,
    val minutes: Int,
    val comebackWins: Int,
    val consistencyScore: Int,
    val currentMomentum: String
)

class ConsistencyScorer {
    fun summarize(sessions: List<WorkoutSessionEntity>): ProgressSummary {
        val workouts = sessions.size
        val minutes = sessions.sumOf { it.durationMinutes }
        val comebackWins = sessions.count { it.title.contains("comeback", ignoreCase = true) || it.title.contains("reset", ignoreCase = true) }
        val score = (workouts * 12 + minutes / 5 + comebackWins * 8).coerceAtMost(100)
        val momentum = when {
            workouts == 0 -> "Ready for the first win"
            score < 25 -> "Restarting gently"
            score < 60 -> "Building momentum"
            else -> "Strong consistency"
        }
        return ProgressSummary(workouts, minutes, comebackWins, score, momentum)
    }
}
