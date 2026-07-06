package com.pivotfit.app.domain.recovery

import com.pivotfit.app.data.models.MuscleGroup

class RecoveryAdvisor {
    fun message(soreness: Set<MuscleGroup>): String =
        if (soreness.isEmpty()) {
            "Move at a pace that feels controlled. Skip anything that causes pain."
        } else {
            "Based on soreness, avoid hard work for ${soreness.joinToString { it.label.lowercase() }} today. For injuries, follow professional medical advice."
        }
}
