package com.eldevazteca.aztecawallet.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eldevazteca.aztecawallet.WalletApplication
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import com.eldevazteca.aztecawallet.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormScreen(
    categoryId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModel.Factory(
            categoryRepository = WalletApplication.instance.categoryRepository
        )
    )
) {
    val formState by viewModel.formState.collectAsState()
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            WalletApplication.instance.categoryRepository.getById(categoryId)?.let {
                viewModel.startEdit(it)
            }
        } else {
            viewModel.startCreate()
        }
    }

    LaunchedEffect(saved) {
        if (saved) {
            onSaved()
            viewModel.resetSaved()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (formState.isEditing) "Editar categoría" else "Nueva categoría",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateFormName(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Text(
                text = "Tipo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = formState.type == TransactionType.EXPENSE,
                    onClick = { viewModel.updateFormType(TransactionType.EXPENSE) },
                    label = { Text("Gasto") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                FilterChip(
                    selected = formState.type == TransactionType.INCOME,
                    onClick = { viewModel.updateFormType(TransactionType.INCOME) },
                    label = { Text("Ingreso") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveCategory() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = formState.name.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Guardar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
