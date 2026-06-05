package com.eldevazteca.aztecawallet.domain.repository

import com.eldevazteca.aztecawallet.data.db.CategoryExpense
import com.eldevazteca.aztecawallet.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<TransactionEntity>>
    suspend fun getById(id: Long): TransactionEntity?
    fun getRecent(limit: Int = 10): Flow<List<TransactionEntity>>
    fun getByAccount(accountId: Long): Flow<List<TransactionEntity>>
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    suspend fun insert(transaction: TransactionEntity): Long
    suspend fun update(transaction: TransactionEntity)
    suspend fun deleteById(id: Long)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double>
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double>
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryExpense>>
}
