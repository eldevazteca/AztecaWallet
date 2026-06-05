package com.eldevazteca.aztecawallet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
        """
    )
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date >= :startDate AND date <= :endDate")
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate")
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE categoryId = :categoryId AND date >= :startDate AND date <= :endDate")
    fun getTotalByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND categoryId = :categoryId 
        AND date >= :startDate AND date <= :endDate
        """
    )
    fun getTotalExpenseByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Double>

    @Query(
        """
        SELECT categoryId, COALESCE(SUM(amount), 0) as total 
        FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate 
        GROUP BY categoryId
        """
    )
    fun getExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategoryExpense>>
}

data class CategoryExpense(
    val categoryId: Long?,
    val total: Double
)
