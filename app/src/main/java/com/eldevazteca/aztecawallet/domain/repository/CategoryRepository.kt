package com.eldevazteca.aztecawallet.domain.repository

import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<CategoryEntity>>
    fun getByType(type: TransactionType): Flow<List<CategoryEntity>>
    suspend fun getById(id: Long): CategoryEntity?
    suspend fun insert(category: CategoryEntity): Long
    suspend fun update(category: CategoryEntity)
    suspend fun deleteById(id: Long)
}
