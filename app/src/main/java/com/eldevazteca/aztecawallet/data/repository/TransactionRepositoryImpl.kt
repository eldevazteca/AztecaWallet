package com.eldevazteca.aztecawallet.data.repository

import com.eldevazteca.aztecawallet.data.db.CategoryExpense
import com.eldevazteca.aztecawallet.data.db.TransactionDao
import com.eldevazteca.aztecawallet.data.db.TransactionEntity
import com.eldevazteca.aztecawallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {
    override fun getAll(): Flow<List<TransactionEntity>> = dao.getAll()
    override suspend fun getById(id: Long): TransactionEntity? = dao.getById(id)
    override fun getRecent(limit: Int): Flow<List<TransactionEntity>> = dao.getRecent(limit)
    override fun getByAccount(accountId: Long): Flow<List<TransactionEntity>> = dao.getByAccount(accountId)
    override fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> = dao.getByDateRange(startDate, endDate)
    override suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)
    override suspend fun update(transaction: TransactionEntity) = dao.update(transaction)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
    override fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double> = dao.getTotalIncome(startDate, endDate)
    override fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double> = dao.getTotalExpense(startDate, endDate)
    override fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryExpense>> = dao.getExpensesByCategory(startDate, endDate)
}
