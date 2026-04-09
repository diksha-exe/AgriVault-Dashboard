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
                AgriVaultUI()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriVaultUI() {

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val transactions = remember {
        mutableStateListOf<TransactionEntity>().apply {
            addAll(DummyData.transactions)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

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

        Spacer(modifier = Modifier.height(8.dp))

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

        Text("Total Expenses: ₹${transactions.sumOf { it.amount }}")

        Spacer(modifier = Modifier.height(8.dp))

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

