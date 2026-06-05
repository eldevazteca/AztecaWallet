package com.eldevazteca.aztecawallet.domain.repository

import com.eldevazteca.aztecawallet.data.db.BudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAll(): Flow<List<BudgetEntity>>
    suspend fun getById(id: Long): BudgetEntity?
    fun getByPeriod(month: Int, year: Int): Flow<List<BudgetEntity>>
    suspend fun getByCategoryAndPeriod(categoryId: Long, month: Int, year: Int): BudgetEntity?
    suspend fun insert(budget: BudgetEntity): Long
    suspend fun update(budget: BudgetEntity)
    suspend fun deleteById(id: Long)
}
