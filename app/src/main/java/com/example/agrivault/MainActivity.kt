package com.example.agrivault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agrivault.data.DummyData
import com.example.agrivault.data.TransactionEntity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgriVaultUI()
        }
    }
}

@Composable
fun AgriVaultUI() {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val transactions = remember {
        mutableStateListOf<TransactionEntity>().apply {
            addAll(com.example.agrivault.data.DummyData.transactions)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = "AgriVault", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            // Add to list (NO validation yet → intentional bug)
            transactions.add(
                TransactionEntity(
                    id = transactions.size, // ❌ bug (intentional)
                    title = title,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    timestamp = System.currentTimeMillis()
                )
            )

            // ❌ not clearing input (intentional bug)

        }) {
            Text("Log Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Balance: ₹${transactions.sumOf { it.amount }}") // ❌ wrong label

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions) { txn ->
                Text("${txn.title} - ₹${txn.amount}")
            }
        }
    }
}