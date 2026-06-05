package com.eldevazteca.aztecawallet

import android.app.Application
import android.util.Log
import com.eldevazteca.aztecawallet.data.db.AppDatabase
import com.eldevazteca.aztecawallet.data.repository.AccountRepositoryImpl
import com.eldevazteca.aztecawallet.data.repository.BudgetRepositoryImpl
import com.eldevazteca.aztecawallet.data.repository.CategoryRepositoryImpl
import com.eldevazteca.aztecawallet.data.repository.TransactionRepositoryImpl
import com.eldevazteca.aztecawallet.domain.repository.AccountRepository
import com.eldevazteca.aztecawallet.domain.repository.BudgetRepository
import com.eldevazteca.aztecawallet.domain.repository.CategoryRepository
import com.eldevazteca.aztecawallet.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var categoryRepository: CategoryRepository
        private set

    lateinit var accountRepository: AccountRepository
        private set

    lateinit var transactionRepository: TransactionRepository
        private set

    lateinit var budgetRepository: BudgetRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        accountRepository = AccountRepositoryImpl(database.accountDao())
        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        budgetRepository = BudgetRepositoryImpl(database.budgetDao())

        verifyDatabase()
    }

    private fun verifyDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val categoryCount = database.categoryDao().countPredefined()
                val accountCount = database.accountDao().getAll().let {
                    // just check if the table exists and is queryable
                    Log.d("AztecaWallet", "DB verificada correctamente. Categorías: $categoryCount")
                }
                Log.d("AztecaWallet", "Base de datos lista para usar")
            } catch (e: Exception) {
                Log.e("AztecaWallet", "Error verificando DB: ${e.message}", e)
            }
        }
    }

    companion object {
        lateinit var instance: WalletApplication
            private set
    }
}
