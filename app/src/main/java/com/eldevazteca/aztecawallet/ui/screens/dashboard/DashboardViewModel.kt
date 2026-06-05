package com.eldevazteca.aztecawallet.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.data.db.TransactionEntity
import com.eldevazteca.aztecawallet.domain.repository.AccountRepository
import com.eldevazteca.aztecawallet.domain.repository.CategoryRepository
import com.eldevazteca.aztecawallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val expensesByCategory: List<Pair<CategoryEntity?, Double>> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            cal.timeInMillis = now
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val monthEnd = cal.timeInMillis

            launch {
                accountRepository.getTotalBalance().collect { balance ->
                    _state.value = _state.value.copy(totalBalance = balance ?: 0.0)
                }
            }
            launch {
                transactionRepository.getTotalIncome(monthStart, monthEnd).collect { income ->
                    _state.value = _state.value.copy(monthlyIncome = income)
                }
            }
            launch {
                transactionRepository.getTotalExpense(monthStart, monthEnd).collect { expense ->
                    _state.value = _state.value.copy(monthlyExpense = expense)
                }
            }
            launch {
                transactionRepository.getRecent(5).collect { recent ->
                    _state.value = _state.value.copy(recentTransactions = recent)
                }
            }
            launch {
                categoryRepository.getAll().collect { cats ->
                    _state.value = _state.value.copy(categories = cats)
                }
            }
            launch {
                transactionRepository.getExpensesByCategory(monthStart, monthEnd).collect { catExpenses ->
                    val catExpensePairs = catExpenses.map { ce ->
                        val cat = ce.categoryId?.let { id ->
                            _state.value.categories.find { it.id == id }
                        }
                        cat to ce.total
                    }.sortedByDescending { it.second }
                    _state.value = _state.value.copy(
                        expensesByCategory = catExpensePairs,
                        isLoading = false
                    )
                }
            }
        }
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val accountRepository: AccountRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(transactionRepository, accountRepository, categoryRepository) as T
        }
    }
}
