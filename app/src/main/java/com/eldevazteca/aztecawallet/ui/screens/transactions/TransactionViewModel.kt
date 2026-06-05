package com.eldevazteca.aztecawallet.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldevazteca.aztecawallet.data.db.AccountEntity
import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.data.db.TransactionEntity
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import com.eldevazteca.aztecawallet.domain.repository.AccountRepository
import com.eldevazteca.aztecawallet.domain.repository.CategoryRepository
import com.eldevazteca.aztecawallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class TransactionListState(
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val currentFilter: TransactionType? = null,
    val isLoading: Boolean = true
)

data class TransactionFormState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val isEditing: Boolean = false,
    val editingId: Long? = null
)

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(TransactionListState())
    val listState: StateFlow<TransactionListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAll(),
                categoryRepository.getAll(),
                accountRepository.getAll()
            ) { transactions, categories, accounts ->
                val filtered = if (_listState.value.currentFilter != null) {
                    transactions.filter { it.type == _listState.value.currentFilter }
                } else transactions

                _listState.value = TransactionListState(
                    transactions = filtered,
                    categories = categories,
                    accounts = accounts,
                    currentFilter = _listState.value.currentFilter,
                    isLoading = false
                )
            }.launchIn(viewModelScope)
        }
    }

    private suspend fun loadFormData() {
        val cats = categoryRepository.getAll().first()
        val accs = accountRepository.getAll().first()
        _formState.value = _formState.value.copy(categories = cats, accounts = accs)
    }

    fun setFilter(type: TransactionType?) {
        _listState.value = _listState.value.copy(currentFilter = type)
        loadTransactions()
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val original = transactionRepository.getById(id)
            transactionRepository.deleteById(id)
            original?.let { tx ->
                val reverseType = if (tx.type == TransactionType.INCOME) TransactionType.EXPENSE else TransactionType.INCOME
                applyBalanceEffect(tx.accountId, tx.amount, reverseType)
            }
        }
    }

    fun startCreate() {
        viewModelScope.launch {
            _formState.value = TransactionFormState()
            loadFormData()
            _saved.value = false
        }
    }

    fun startEdit(transaction: TransactionEntity) {
        viewModelScope.launch {
            _formState.value = TransactionFormState(
                amount = formatAmount(transaction.amount),
                type = transaction.type,
                categoryId = transaction.categoryId,
                accountId = transaction.accountId,
                note = transaction.note,
                date = transaction.date,
                isEditing = true,
                editingId = transaction.id
            )
            loadFormData()
            _saved.value = false
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else String.format("%.2f", amount)
    }

    fun updateFormAmount(amount: String) { _formState.value = _formState.value.copy(amount = amount) }
    fun updateFormType(type: TransactionType) { _formState.value = _formState.value.copy(type = type) }
    fun updateFormCategory(id: Long?) { _formState.value = _formState.value.copy(categoryId = id) }
    fun updateFormAccount(id: Long?) { _formState.value = _formState.value.copy(accountId = id) }
    fun updateFormNote(note: String) { _formState.value = _formState.value.copy(note = note) }
    fun updateFormDate(date: Long) { _formState.value = _formState.value.copy(date = date) }

    fun saveTransaction() {
        val form = _formState.value
        val amount = form.amount.toDoubleOrNull() ?: return
        if (amount <= 0) return

        viewModelScope.launch {
            if (form.isEditing) {
                val original = transactionRepository.getById(form.editingId!!)
                val entity = TransactionEntity(
                    id = form.editingId,
                    amount = amount,
                    type = form.type,
                    categoryId = form.categoryId,
                    accountId = form.accountId,
                    date = form.date,
                    note = form.note
                )
                transactionRepository.update(entity)
                original?.let { tx ->
                    val reverseType = if (tx.type == TransactionType.INCOME) TransactionType.EXPENSE else TransactionType.INCOME
                    applyBalanceEffect(tx.accountId, tx.amount, reverseType)
                }
                applyBalanceEffect(form.accountId, amount, form.type)
            } else {
                val entity = TransactionEntity(
                    amount = amount,
                    type = form.type,
                    categoryId = form.categoryId,
                    accountId = form.accountId,
                    date = form.date,
                    note = form.note
                )
                transactionRepository.insert(entity)
                applyBalanceEffect(form.accountId, amount, form.type)
            }
            _saved.value = true
            _formState.value = TransactionFormState()
        }
    }

    private suspend fun applyBalanceEffect(accountId: Long?, amount: Double, type: TransactionType) {
        if (accountId == null) return
        val change = if (type == TransactionType.INCOME) amount else -amount
        accountRepository.updateBalance(accountId, change)
    }

    fun resetSaved() { _saved.value = false }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val accountRepository: AccountRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionViewModel(transactionRepository, categoryRepository, accountRepository) as T
        }
    }
}
