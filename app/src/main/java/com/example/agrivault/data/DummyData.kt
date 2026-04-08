package com.example.agrivault.data

object DummyData {

    val transactions = listOf(
        TransactionEntity(
            id = 1,
            title = "Seeds",
            amount = 500.0,
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 2,
            title = "Fertilizer",
            amount = 800.0,
            timestamp = System.currentTimeMillis()
        )
    )
}