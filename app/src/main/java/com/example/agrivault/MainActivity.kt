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
import com.example.agrivault.ui.AgriVaultPieChart
import com.example.agrivault.utils.CsvExporter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import androidx.activity.result.contract.ActivityResultContracts
import com.example.agrivault.utils.BackupUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AgriVaultTheme {
                val viewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(
                        (LocalContext.current.applicationContext as AgriVaultApplication).repository
                    )
                )

                val scope = rememberCoroutineScope()

                // Backup Launcher
                val createDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val snapshot = viewModel.getBackupSnapshot()
                            val json = BackupUtility.serializeBackup(snapshot)
                            withContext(Dispatchers.IO) {
                                contentResolver.openOutputStream(it)?.use { stream ->
                                    stream.write(json.toByteArray())
                                }
                            }
                        }
                    }
                }

                // Restore Launcher
                val openDocumentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            val json = withContext(Dispatchers.IO) {
                                contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                                    reader.readText()
                                }
                            }
                            json?.let {
                                val data = BackupUtility.deserializeBackup(it)
                                data?.let { backup ->
                                    viewModel.restoreFromBackup(backup)
                                }
                            }
                        }
                    }
                }

                AgriVaultUI(viewModel, onBackup = {
                    createDocumentLauncher.launch("agrivault_backup.json")
                }, onRestore = {
                    openDocumentLauncher.launch(arrayOf("application/json"))
                })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AgriVaultUI(
    viewModel: TransactionViewModel,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {

    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect transactions from ViewModel as State
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle()

    // Grouping logic
    val groupedTransactions = remember(transactions) {
        transactions.groupBy { formatDateHeader(it.timestamp) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "AgriVault Dashboard", style = MaterialTheme.typography.headlineLarge)
                
                // Backup/Restore Icons
                Row {
                    IconButton(onClick = onBackup) {
                        Icon(Icons.Default.Upload, contentDescription = "Backup")
                    }
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Download, contentDescription = "Restore")
                    }
                }
            }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Spending: ₹${transactions.sumOf { it.amount }}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = {
                    CsvExporter.exportTransactionsToCsv(context, transactions)
                }) {
                    Text("Export Reports")
                }
            }

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
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.delete(txn)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted ${txn.title}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.insert(txn)
                                        }
                                    }
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = { DismissBackground(dismissState) },
                            content = { TransactionItem(txn) },
                            enableDismissFromStartToEnd = false
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> Color(0xFFBA1A1A)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White
        )
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
