package com.eldevazteca.aztecawallet.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eldevazteca.aztecawallet.WalletApplication
import com.eldevazteca.aztecawallet.data.db.CategoryEntity
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import com.eldevazteca.aztecawallet.ui.components.AppTopBar
import com.eldevazteca.aztecawallet.ui.components.CategoryIcon
import com.eldevazteca.aztecawallet.ui.components.DeleteConfirmDialog
import com.eldevazteca.aztecawallet.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModel.Factory(
            categoryRepository = WalletApplication.instance.categoryRepository
        )
    )
) {
    val state by viewModel.state.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }

    if (showForm) {
        CategoryFormScreen(
            onBack = { showForm = false },
            onSaved = { showForm = false }
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Categorías",
                    onBack = onBack
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
                }
            }
        ) { padding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.categories.isEmpty()) {
                EmptyState("No hay categorías")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            onDelete = {
                                if (!category.isPredefined) {
                                    deleteTargetId = category.id
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog && deleteTargetId != null) {
            DeleteConfirmDialog(
                message = "¿Eliminar esta categoría?",
                onConfirm = {
                    viewModel.deleteCategory(deleteTargetId!!)
                    showDeleteDialog = false
                    deleteTargetId = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    deleteTargetId = null
                }
            )
        }
    }
}

@Composable
private fun CategoryCard(category: CategoryEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(iconName = category.icon, color = category.color)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (category.type == TransactionType.INCOME) "Ingreso" else "Gasto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!category.isPredefined) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
