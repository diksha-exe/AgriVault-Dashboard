package com.example.agrivault.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<AgriculturalCategory>> = categoryDao.getAllCategories()

    suspend fun insert(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertCategory(category: AgriculturalCategory) {
        categoryDao.insertCategory(category)
    }

    suspend fun getTransactionById(id: Int): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getBackupSnapshot(): BackupData {
        return BackupData(
            transactions = transactionDao.getAllTransactionsSnapshot(),
            categories = categoryDao.getAllCategoriesSnapshot()
        )
    }

    suspend fun restoreFromBackup(data: BackupData) {
        // We'll use a simple approach here since we're in the repository.
        // For actual Room transactions, we'd need the database instance.
        // Assuming the caller handles threading correctly.
        transactionDao.deleteAllTransactions()
        categoryDao.deleteAllCategories()
        
        data.categories.forEach { categoryDao.insertCategory(it) }
        data.transactions.forEach { transactionDao.insertTransaction(it) }
    }
}


