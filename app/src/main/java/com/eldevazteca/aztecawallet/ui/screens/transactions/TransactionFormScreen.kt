package com.eldevazteca.aztecawallet.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eldevazteca.aztecawallet.WalletApplication
import com.eldevazteca.aztecawallet.domain.model.TransactionType
import com.eldevazteca.aztecawallet.ui.components.AppTopBar
import com.eldevazteca.aztecawallet.ui.components.FormatDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    transactionId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModel.Factory(
            transactionRepository = WalletApplication.instance.transactionRepository,
            categoryRepository = WalletApplication.instance.categoryRepository,
            accountRepository = WalletApplication.instance.accountRepository
        )
    )
) {
    val formState by viewModel.formState.collectAsState()
    val saved by viewModel.saved.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            WalletApplication.instance.transactionRepository.getById(transactionId)?.let {
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
                title = if (formState.isEditing) "Editar transacción" else "Nueva transacción",
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
            // Type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = formState.type == TransactionType.EXPENSE,
                    onClick = { viewModel.updateFormType(TransactionType.EXPENSE) },
                    label = { Text("Gasto") },
                    modifier = Modifier.weight(1f),
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
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Amount input
            OutlinedTextField(
                value = formState.amount,
                onValueChange = { viewModel.updateFormAmount(it) },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Accounts
            if (formState.accounts.isNotEmpty()) {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    formState.accounts.forEach { account ->
                        FilterChip(
                            selected = formState.accountId == account.id,
                            onClick = { viewModel.updateFormAccount(account.id) },
                            label = { Text(account.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Categories grid
            val filteredCategories = formState.categories.filter { it.type == formState.type }
            if (filteredCategories.isNotEmpty()) {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredCategories.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { category ->
                                val isSelected = formState.categoryId == category.id
                                val catColor = Color(category.color)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) catColor.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) catColor else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.updateFormCategory(category.id) }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(catColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (category.icon) {
                                                "restaurant" -> "F"
                                                "directions_car" -> "C"
                                                "home" -> "H"
                                                "local_hospital" -> "+"
                                                "movie" -> "M"
                                                "school" -> "E"
                                                "checkroom" -> "R"
                                                "receipt" -> "Rc"
                                                "payments" -> "$"
                                                "computer" -> "</>"
                                                "trending_up" -> "^"
                                                else -> category.icon.take(2).uppercase()
                                            },
                                            color = catColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Date
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.padding(start = 12.dp))
                    Text(
                        text = FormatDate(formState.date),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Note
            OutlinedTextField(
                value = formState.note,
                onValueChange = { viewModel.updateFormNote(it) },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = formState.amount.toDoubleOrNull() != null && (formState.amount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Guardar transacción",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        val zoneId = ZoneId.systemDefault()
        val initialUtcMillis = Instant.ofEpochMilli(formState.date)
            .atZone(zoneId)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtcMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val localMillis = Instant.ofEpochMilli(utcMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .atStartOfDay(zoneId)
                            .toInstant()
                            .toEpochMilli()
                        viewModel.updateFormDate(localMillis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
