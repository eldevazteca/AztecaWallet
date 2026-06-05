package com.eldevazteca.aztecawallet.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldevazteca.aztecawallet.data.db.AccountEntity
import com.eldevazteca.aztecawallet.domain.model.AccountType
import com.eldevazteca.aztecawallet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountsState(
    val accounts: List<AccountEntity> = emptyList(),
    val isLoading: Boolean = true
)

data class AccountFormState(
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val balance: String = "0",
    val icon: String = "account_balance_wallet",
    val color: Long = 0xFF6200EE,
    val isEditing: Boolean = false,
    val editingId: Long? = null
)

class AccountsViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountsState())
    val state: StateFlow<AccountsState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAll().collect { accounts ->
                _state.value = AccountsState(accounts = accounts, isLoading = false)
            }
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            accountRepository.deleteById(id)
        }
    }

    fun startCreate() {
        _formState.value = AccountFormState()
        _saved.value = false
    }

    fun startEdit(account: AccountEntity) {
        _formState.value = AccountFormState(
            name = account.name,
            type = account.type,
            balance = if (account.balance == account.balance.toLong().toDouble()) {
                account.balance.toLong().toString()
            } else account.balance.toString(),
            icon = account.icon,
            color = account.color,
            isEditing = true,
            editingId = account.id
        )
        _saved.value = false
    }

    fun updateFormName(name: String) { _formState.value = _formState.value.copy(name = name) }
    fun updateFormType(type: AccountType) { _formState.value = _formState.value.copy(type = type) }
    fun updateFormBalance(balance: String) { _formState.value = _formState.value.copy(balance = balance) }
    fun updateFormIcon(icon: String) { _formState.value = _formState.value.copy(icon = icon) }
    fun updateFormColor(color: Long) { _formState.value = _formState.value.copy(color = color) }

    fun saveAccount() {
        val form = _formState.value
        if (form.name.isBlank()) return
        val balance = form.balance.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            val entity = AccountEntity(
                id = form.editingId ?: 0,
                name = form.name,
                type = form.type,
                balance = balance,
                icon = form.icon,
                color = form.color
            )
            if (form.isEditing) {
                accountRepository.update(entity)
            } else {
                accountRepository.insert(entity)
            }
            _saved.value = true
            _formState.value = AccountFormState()
        }
    }

    fun resetSaved() { _saved.value = false }

    class Factory(
        private val accountRepository: AccountRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountsViewModel(accountRepository) as T
        }
    }
}
