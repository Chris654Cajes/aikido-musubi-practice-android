package com.aikido.musubi

import android.app.Application
import androidx.room.Room
import com.aikido.musubi.data.database.AikidoDatabase
import com.aikido.musubi.data.repository.PracticeRepository

class AikidoApp : Application() {

    val database: AikidoDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AikidoDatabase::class.java,
            AikidoDatabase.DATABASE_NAME
        ).build()
    }

    val repository: PracticeRepository by lazy {
        PracticeRepository(database)
    }
}
