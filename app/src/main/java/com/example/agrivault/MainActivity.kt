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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.agrivault.data.TransactionEntity
import com.example.agrivault.ui.theme.AgriVaultTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrivault.ui.TransactionViewModel
import com.example.agrivault.ui.TransactionViewModelFactory
import com.example.agrivault.AgriVaultApplication
import androidx.compose.ui.platform.LocalContext
import com.example.agrivault.ui.AgriVaultPieChart

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgriVaultUI(viewModel: TransactionViewModel) {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    // Collect transactions from ViewModel as State
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle()

    // Grouping logic
    val groupedTransactions = remember(transactions) {
        transactions.groupBy { formatDateHeader(it.timestamp) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(text = "AgriVault Dashboard", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Pie Chart Visualization
        if (spendingByCategory.isNotEmpty()) {
            AgriVaultPieChart(
                spendingData = spendingByCategory,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountValue = amount.toDoubleOrNull()
                if (title.isNotBlank() && amountValue != null && amountValue > 0) {
                    viewModel.insert(
                        TransactionEntity(
                            title = title,
                            amount = amountValue,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    title = ""
                    amount = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Expense")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Total Spending: ₹${transactions.sumOf { it.amount }}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            groupedTransactions.forEach { (date, txns) ->
                stickyHeader {
                    Text(
                        text = date,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                items(txns, key = { it.id }) { txn ->
                    TransactionItem(txn)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(txn: TransactionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = txn.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                        .format(java.time.Instant.ofEpochMilli(txn.timestamp).atZone(java.time.ZoneId.systemDefault())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "₹${txn.amount}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun formatDateHeader(timestamp: Long): String {
    val date = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val now = LocalDate.now()
    
    return when {
        date.isEqual(now) -> "Today"
        date.isEqual(now.minusDays(1)) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }
}

