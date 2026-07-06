package com.pivotfit.app

import android.app.Application
import com.pivotfit.app.data.repositories.PivotRepository

class PivotFitApp : Application() {
    val repository by lazy { PivotRepository(this) }
}
