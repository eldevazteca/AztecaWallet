package com.eldevazteca.aztecawallet.ui.screens.accounts

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
import com.eldevazteca.aztecawallet.domain.model.AccountType
import com.eldevazteca.aztecawallet.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFormScreen(
    accountId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModel.Factory(
            accountRepository = WalletApplication.instance.accountRepository
        )
    )
) {
    val formState by viewModel.formState.collectAsState()
    val saved by viewModel.saved.collectAsState()

    LaunchedEffect(accountId) {
        if (accountId != null) {
            WalletApplication.instance.accountRepository.getById(accountId)?.let {
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
                title = if (formState.isEditing) "Editar cuenta" else "Nueva cuenta",
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
                text = "Tipo de cuenta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountType.entries.forEach { type ->
                    FilterChip(
                        selected = formState.type == type,
                        onClick = { viewModel.updateFormType(type) },
                        label = {
                            Text(
                                when (type) {
                                    AccountType.CASH -> "Efectivo"
                                    AccountType.DEBIT -> "Débito"
                                    AccountType.CREDIT -> "Crédito"
                                    AccountType.SAVINGS -> "Ahorros"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            OutlinedTextField(
                value = formState.balance,
                onValueChange = { viewModel.updateFormBalance(it) },
                label = { Text("Saldo inicial") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveAccount() },
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
