package com.fitin60.app

import android.app.Application
import com.fitin60.app.data.local.Fitin60Database
import com.fitin60.app.data.repository.Fitin60Repository

class Fitin60App : Application() {

    val database: Fitin60Database by lazy { Fitin60Database.getInstance(this) }

    val repository: Fitin60Repository by lazy {
        Fitin60Repository(
            programDao = database.programDao(),
            dayPlanDao = database.dayPlanDao(),
            weeklyCheckinDao = database.weeklyCheckinDao(),
            appContext = applicationContext,
        )
    }
}
