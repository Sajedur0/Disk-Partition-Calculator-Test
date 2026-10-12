package com.diskcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Radii

/** Confirmation dialog shown before closing the app. */
@Composable
fun QuitDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Radii.dialog,
        containerColor = colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colorScheme.errorContainer),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    tint = colorScheme.onErrorContainer,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(26.dp),
                )
            }
        },
        title = {
            Text(
                text = AppStrings.Quit.TITLE,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = AppStrings.Quit.MESSAGE,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error,
                    contentColor = colorScheme.onError,
                ),
            ) {
                Text(text = AppStrings.Quit.CONFIRM)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = AppStrings.Quit.CANCEL)
            }
        },
    )
}
