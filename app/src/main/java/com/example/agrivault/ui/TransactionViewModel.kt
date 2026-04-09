package com.example.agrivault.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agrivault.data.TransactionEntity
import com.example.agrivault.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.example.agrivault.ui.CategorySpending
import com.example.agrivault.data.BackupData

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    // Expose all transactions as a StateFlow for the UI to collect
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calculate spending per category
    val spendingByCategory: StateFlow<List<CategorySpending>> = allTransactions.map { transactions ->
        if (transactions.isEmpty()) return@map emptyList()
        
        // For demo: group by title if categoryId is null, otherwise by category
        // In a real app, we'd join with AgriculturalCategory
        val grouped = transactions.groupBy { it.title }
        val colors = listOf(Color(0xFF2E7D32), Color(0xFF53634F), Color(0xFF386566), Color(0xFF72796F), Color(0xFFBA1A1A))
        
        grouped.entries.mapIndexed { index, entry ->
            CategorySpending(
                categoryName = entry.key,
                amount = entry.value.sumOf { it.amount },
                color = colors[index % colors.size]
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insert(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insert(transaction)
        }
    }

    fun delete(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    suspend fun getBackupSnapshot(): BackupData {
        return repository.getBackupSnapshot()
    }

    fun restoreFromBackup(data: BackupData) {
        viewModelScope.launch {
            repository.restoreFromBackup(data)
        }
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
