package com.eldevazteca.aztecawallet.data.repository

import com.eldevazteca.aztecawallet.data.db.CategoryDao
import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import com.eldevazteca.aztecawallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {
    override fun getAll(): Flow<List<CategoryEntity>> = dao.getAll()
    override fun getByType(type: TransactionType): Flow<List<CategoryEntity>> = dao.getByType(type)
    override suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)
    override suspend fun insert(category: CategoryEntity): Long = dao.insert(category)
    override suspend fun update(category: CategoryEntity) = dao.update(category)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
