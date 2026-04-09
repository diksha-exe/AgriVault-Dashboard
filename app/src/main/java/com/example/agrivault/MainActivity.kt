package com.example.agrivault

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.DismissValue
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.agrivault.data.DummyData
import com.example.agrivault.data.TransactionEntity
import com.example.agrivault.ui.theme.AgriVaultTheme
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriVaultUI(
    viewModel: TransactionViewModel,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val transactions = remember {
        mutableStateListOf<TransactionEntity>().apply {
            addAll(DummyData.transactions)
        }
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

        // ✅ Localization
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )

            Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.title)) }
        )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.amount)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val amountValue = amount.toDoubleOrNull()

            if (title.isBlank()) {
                Toast.makeText(context, "Enter title", Toast.LENGTH_SHORT).show()
                return@Button
            }

            if (amountValue == null || amountValue <= 0) {
                Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@Button
            }

            transactions.add(
                TransactionEntity(
                    id = (transactions.maxOfOrNull { it.id } ?: 0) + 1,
                    title = title,
                    amount = amountValue,
                    date = System.currentTimeMillis()
                )
            )

            title = ""
            amount = ""

        }) {
            Text(stringResource(R.string.log_expense))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Export CSV
        Button(onClick = {
            exportToCSV(context, transactions)
            Toast.makeText(context, "CSV Exported", Toast.LENGTH_SHORT).show()
        }) {
            Text("Export CSV")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Backup
        Button(onClick = {
            backupData(context, transactions)
            Toast.makeText(context, "Backup Saved", Toast.LENGTH_SHORT).show()
        }) {
            Text("Backup")
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

        LazyColumn {
            items(transactions, key = { it.id }) { txn ->

                val dismissState = rememberDismissState(
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToStart) {
                            transactions.remove(txn)
                        }
                        true
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {},
                    dismissContent = {
                        Text(
                            "${txn.title} - ₹${txn.amount}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                )
            }
        }
    }
}

// ✅ CSV Export
fun exportToCSV(context: Context, transactions: List<TransactionEntity>) {
    val file = File(context.getExternalFilesDir(null), "transactions.csv")

    file.printWriter().use { writer ->
        writer.println("Title,Amount,Date")
        transactions.forEach {
            writer.println("${it.title},${it.amount},${it.date}")
        }
    }
}

// ✅ Backup JSON
fun backupData(context: Context, transactions: List<TransactionEntity>) {
    val file = File(context.getExternalFilesDir(null), "backup.json")
    val json = Gson().toJson(transactions)
    file.writeText(json)
}

