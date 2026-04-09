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
import com.example.agrivault.ui.theme.AgriVaultTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgriVaultTheme {
                AgriVaultUI()
            }
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
            val amountValue = amount.toDoubleOrNull()
            if (title.isNotBlank() && amountValue != null && amountValue > 0) {
                transactions.add(
                    TransactionEntity(
                        id = (transactions.maxOfOrNull { it.id } ?: 0) + 1,
                        title = title,
                        amount = amountValue,
                        timestamp = System.currentTimeMillis()
                    )
                )
                // Clear input fields after successful log
                title = ""
                amount = ""
            }
        }) {
            Text("Log Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total Expenses: ₹${transactions.sumOf { it.amount }}")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(transactions) { txn ->
                Text("${txn.title} - ₹${txn.amount}")
            }
        }
    }
}