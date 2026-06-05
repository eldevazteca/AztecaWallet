package com.eldevazteca.aztecawallet.domain.repository

import com.eldevazteca.aztecawallet.data.db.AccountEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAll(): Flow<List<AccountEntity>>
    suspend fun getById(id: Long): AccountEntity?
    fun getByIdFlow(id: Long): Flow<AccountEntity?>
    suspend fun insert(account: AccountEntity): Long
    suspend fun update(account: AccountEntity)
    suspend fun deleteById(id: Long)
    suspend fun updateBalance(accountId: Long, amount: Double)
    fun getTotalBalance(): Flow<Double?>
}
