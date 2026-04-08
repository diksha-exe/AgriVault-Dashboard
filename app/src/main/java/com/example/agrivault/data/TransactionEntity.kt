package com.example.agrivault.data

data class TransactionEntity(
    val id: Int,
    val title: String,
    val amount: Double,
    val timestamp: Long
)