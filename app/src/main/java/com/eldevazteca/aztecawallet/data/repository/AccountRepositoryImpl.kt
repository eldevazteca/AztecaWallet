package com.eldevazteca.aztecawallet.data.repository

import com.eldevazteca.aztecawallet.data.db.AccountDao
import com.eldevazteca.aztecawallet.data.db.AccountEntity
import com.eldevazteca.aztecawallet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(private val dao: AccountDao) : AccountRepository {
    override fun getAll(): Flow<List<AccountEntity>> = dao.getAll()
    override suspend fun getById(id: Long): AccountEntity? = dao.getById(id)
    override fun getByIdFlow(id: Long): Flow<AccountEntity?> = dao.getByIdFlow(id)
    override suspend fun insert(account: AccountEntity): Long = dao.insert(account)
    override suspend fun update(account: AccountEntity) = dao.update(account)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
    override suspend fun updateBalance(accountId: Long, amount: Double) = dao.updateBalance(accountId, amount)
    override fun getTotalBalance(): Flow<Double?> = dao.getTotalBalance()
}
