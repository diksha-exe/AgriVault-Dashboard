package com.example.agrivault.data

data class BackupData(
    val transactions: List<TransactionEntity>,
    val categories: List<AgriculturalCategory>,
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: Int = 1
)
