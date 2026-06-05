package com.eldevazteca.aztecawallet.data.repository

import com.eldevazteca.aztecawallet.data.db.BudgetDao
import com.eldevazteca.aztecawallet.data.db.BudgetEntity
import com.eldevazteca.aztecawallet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class BudgetRepositoryImpl(private val dao: BudgetDao) : BudgetRepository {
    override fun getAll(): Flow<List<BudgetEntity>> = dao.getAll()
    override suspend fun getById(id: Long): BudgetEntity? = dao.getById(id)
    override fun getByPeriod(month: Int, year: Int): Flow<List<BudgetEntity>> = dao.getByPeriod(month, year)
    override suspend fun getByCategoryAndPeriod(categoryId: Long, month: Int, year: Int): BudgetEntity? = dao.getByCategoryAndPeriod(categoryId, month, year)
    override suspend fun insert(budget: BudgetEntity): Long = dao.insert(budget)
    override suspend fun update(budget: BudgetEntity) = dao.update(budget)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
