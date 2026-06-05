package com.eldevazteca.aztecawallet.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eldevazteca.aztecawallet.WalletApplication
import com.eldevazteca.aztecawallet.data.db.AccountEntity
import com.eldevazteca.aztecawallet.domain.model.AccountType
import com.eldevazteca.aztecawallet.ui.components.AmountText
import com.eldevazteca.aztecawallet.ui.components.AppTopBar
import com.eldevazteca.aztecawallet.ui.components.CategoryIcon
import com.eldevazteca.aztecawallet.ui.components.DeleteConfirmDialog
import com.eldevazteca.aztecawallet.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModel.Factory(
            accountRepository = WalletApplication.instance.accountRepository
        )
    )
) {
    val state by viewModel.state.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }

    if (showForm) {
        AccountFormScreen(
            accountId = null,
            onBack = { showForm = false },
            onSaved = { showForm = false }
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Cuentas",
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
                    Icon(Icons.Default.Add, contentDescription = "Agregar cuenta")
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
            } else if (state.accounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState("No hay cuentas. Agrega una.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.accounts, key = { it.id }) { account ->
                        AccountCard(
                            account = account,
                            onDelete = {
                                deleteTargetId = account.id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog && deleteTargetId != null) {
            DeleteConfirmDialog(
                message = "¿Eliminar esta cuenta?",
                onConfirm = {
                    viewModel.deleteAccount(deleteTargetId!!)
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
private fun AccountCard(account: AccountEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(account.color).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (account.type) {
                        AccountType.CASH -> "E"
                        AccountType.DEBIT -> "D"
                        AccountType.CREDIT -> "C"
                        AccountType.SAVINGS -> "A"
                    },
                    color = Color(account.color),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (account.type) {
                        AccountType.CASH -> "Efectivo"
                        AccountType.DEBIT -> "Débito"
                        AccountType.CREDIT -> "Crédito"
                        AccountType.SAVINGS -> "Ahorros"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AmountText(amount = account.balance, fontSize = 16.sp)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
