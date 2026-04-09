package com.example.agrivault

import android.app.Application
import com.example.agrivault.data.AppDatabase
import com.example.agrivault.data.TransactionRepository

class AgriVaultApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
