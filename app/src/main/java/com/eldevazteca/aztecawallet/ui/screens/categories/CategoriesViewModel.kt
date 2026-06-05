package com.eldevazteca.aztecawallet.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import com.eldevazteca.aztecawallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriesState(
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true
)

data class CategoryFormState(
    val name: String = "",
    val icon: String = "category",
    val color: Long = 0xFF9E9E9E,
    val type: TransactionType = TransactionType.EXPENSE,
    val isEditing: Boolean = false,
    val editingId: Long? = null
)

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(CategoryFormState())
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                _state.value = CategoriesState(categories = categories, isLoading = false)
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.deleteById(id)
        }
    }

    fun startCreate() {
        _formState.value = CategoryFormState()
        _saved.value = false
    }

    fun startEdit(category: CategoryEntity) {
        _formState.value = CategoryFormState(
            name = category.name,
            icon = category.icon,
            color = category.color,
            type = category.type,
            isEditing = true,
            editingId = category.id
        )
        _saved.value = false
    }

    fun updateFormName(name: String) { _formState.value = _formState.value.copy(name = name) }
    fun updateFormIcon(icon: String) { _formState.value = _formState.value.copy(icon = icon) }
    fun updateFormColor(color: Long) { _formState.value = _formState.value.copy(color = color) }
    fun updateFormType(type: TransactionType) { _formState.value = _formState.value.copy(type = type) }

    fun saveCategory() {
        val form = _formState.value
        if (form.name.isBlank()) return

        viewModelScope.launch {
            val entity = CategoryEntity(
                id = form.editingId ?: 0,
                name = form.name,
                icon = form.icon,
                color = form.color,
                type = form.type,
                isPredefined = false
            )
            if (form.isEditing) {
                categoryRepository.update(entity)
            } else {
                categoryRepository.insert(entity)
            }
            _saved.value = true
            _formState.value = CategoryFormState()
        }
    }

    fun resetSaved() { _saved.value = false }

    class Factory(
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoriesViewModel(categoryRepository) as T
        }
    }
}
