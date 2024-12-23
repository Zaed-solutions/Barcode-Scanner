package com.zaed.barcodescanner.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.barcodescanner.R

@Composable
fun ConfirmDeleteDialog(
    modifier: Modifier = Modifier,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.are_you_sure_you_want_to_delete_this, label),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.this_action_cannot_be_undone),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .padding(end = 8.dp)
                    .weight(1f),
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .weight(1f),
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        }
    }
}

@Composable
fun ConfirmNavigateToLoginDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.you_need_to_login_first_to_continue),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.some_operation_requires_login),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .padding(end = 8.dp)
                    .weight(1f),
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(R.string.dismiss))
            }
            Button(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .weight(1f),
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.login))
            }
        }
    }
}
@Composable
fun ConfirmDeleteAllFoldersDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.there_are_folders_that_have_not_been_uploaded_yet),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.are_you_sure_you_want_to_delete_all_folders),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .padding(end = 8.dp)
                    .weight(1f),
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(R.string.no))
            }
            Button(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .weight(1f),
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.yes))
            }
        }
    }
}
@Composable
fun EnterBarCodeDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val context = LocalContext.current
    var barcode by remember { mutableStateOf("") }
    var isBarcodeValid by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.enter_barcode),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            value =  barcode,
            onValueChange = {
                barcode = it
            },
            label = { Text(text = stringResource(R.string.barcode)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isBarcodeValid,
            supportingText = {
                if (!isBarcodeValid) {
                    Text(text = errorMessage)
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (barcode.isEmpty()) {
                        isBarcodeValid = false
                        errorMessage = context.getString(R.string.barcode_cannot_be_empty)
                    }else if(barcode.length<7){
                        isBarcodeValid = false
                        errorMessage =
                            context.getString(R.string.barcode_must_be_at_least_7_characters)
                    } else {
                        isBarcodeValid = true
                        onConfirm(barcode)
                    }
                }
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .padding(end = 8.dp)
                    .weight(1f),
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
            Button(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .weight(1f),
                onClick = {
                    if (barcode.isEmpty()) {
                        isBarcodeValid = false
                        errorMessage = context.getString(R.string.barcode_cannot_be_empty)
                    }else if(barcode.length<7){
                        isBarcodeValid = false
                        errorMessage =
                            context.getString(R.string.barcode_must_be_at_least_7_characters)
                    } else {
                        isBarcodeValid = true
                        onConfirm(barcode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.create))
            }
        }
    }
}
