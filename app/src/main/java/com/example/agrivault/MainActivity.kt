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
import com.example.agrivault.data.TransactionEntity
import com.example.agrivault.ui.theme.AgriVaultTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrivault.ui.TransactionViewModel
import com.example.agrivault.ui.TransactionViewModelFactory
import com.example.agrivault.AgriVaultApplication
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgriVaultTheme {
                // Initialize ViewModel with the repository from Application class
                val viewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(
                        (LocalContext.current.applicationContext as AgriVaultApplication).repository
                    )
                )
                AgriVaultUI(viewModel)
            }
        }
    }
}

@Composable
fun AgriVaultUI(viewModel: TransactionViewModel) {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    // Collect transactions from ViewModel as State
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()

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
                // Add via ViewModel
                viewModel.insert(
                    TransactionEntity(
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
            items(transactions, key = { it.id }) { txn ->
                Text("${txn.title} - ₹${txn.amount}")
            }
        }
    }
}

