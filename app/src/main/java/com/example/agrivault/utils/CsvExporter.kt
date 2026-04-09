package com.example.agrivault.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.agrivault.data.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    fun exportTransactionsToCsv(context: Context, transactions: List<TransactionEntity>) {
        val csvHeader = "ID,Title,Amount,Date\n"
        val csvContent = StringBuilder(csvHeader)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        transactions.forEach { txn ->
            val date = dateFormat.format(Date(txn.timestamp))
            csvContent.append("${txn.id},\"${txn.title}\",${txn.amount},\"$date\"\n")
        }

        try {
            // Save to internal cache directory
            val fileName = "AgriVault_Report_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            outputStream.write(csvContent.toString().toByteArray())
            outputStream.close()

            // Share the file
            shareCsvFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareCsvFile(context: Context, file: File) {
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export Report Via"))
    }
}
