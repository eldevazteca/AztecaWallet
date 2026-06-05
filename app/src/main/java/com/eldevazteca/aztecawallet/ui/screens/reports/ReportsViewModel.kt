package com.eldevazteca.aztecawallet.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.domain.repository.CategoryRepository
import com.eldevazteca.aztecawallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReportsState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val expensesByCategory: List<CategoryWithExpense> = emptyList(),
    val isLoading: Boolean = true,
    val currentMonth: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH)
)

data class CategoryWithExpense(
    val category: CategoryEntity?,
    val total: Double,
    val percentage: Float = 0f
)

class ReportsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    private val calendar = Calendar.getInstance()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            refreshData()
        }
    }

    fun goToPreviousMonth() {
        val state = _state.value
        calendar.set(state.year, state.month - 1, 1)
        _state.value = state.copy(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
        refreshData()
    }

    fun goToNextMonth() {
        val state = _state.value
        calendar.set(state.year, state.month + 1, 1)
        _state.value = state.copy(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            val state = _state.value
            calendar.set(state.year, state.month, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val monthEnd = calendar.timeInMillis

            val monthNames = listOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )

            combine(
                transactionRepository.getTotalIncome(monthStart, monthEnd),
                transactionRepository.getTotalExpense(monthStart, monthEnd),
                transactionRepository.getExpensesByCategory(monthStart, monthEnd),
                categoryRepository.getAll()
            ) { income, expense, catExpenses, cats ->
                val total = catExpenses.sumOf { it.total }
                val withPercentages = catExpenses.map { ce ->
                    val cat = ce.categoryId?.let { id -> cats.find { it.id == id } }
                    CategoryWithExpense(
                        category = cat,
                        total = ce.total,
                        percentage = if (total > 0) (ce.total / total * 100).toFloat() else 0f
                    )
                }.sortedByDescending { it.total }

                _state.value = state.copy(
                    monthlyIncome = income,
                    monthlyExpense = expense,
                    expensesByCategory = withPercentages,
                    isLoading = false,
                    currentMonth = monthNames[state.month]
                )
            }.launchIn(viewModelScope)
        }
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(transactionRepository, categoryRepository) as T
        }
    }
}
