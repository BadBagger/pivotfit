package com.pivotfit.app.domain.progression

import com.pivotfit.app.data.models.RpeRating

class ProgressionAdvisor {
    fun nextStep(rpe: RpeRating, pain: Boolean, ranOutOfTime: Boolean): String = when {
        pain -> "Next time: reduce volume and choose a friendlier swap."
        ranOutOfTime -> "Next time: keep the same exercises but shorter blocks."
        rpe == RpeRating.Easy -> "Next time: add a few reps, a set, or a little weight."
        rpe == RpeRating.TooHard -> "Next time: reduce reps or choose easier alternatives."
        else -> "Next time: repeat this level and build confidence."
    }
}
