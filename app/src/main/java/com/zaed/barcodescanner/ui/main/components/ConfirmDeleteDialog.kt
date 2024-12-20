package com.zaed.barcodescanner.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
