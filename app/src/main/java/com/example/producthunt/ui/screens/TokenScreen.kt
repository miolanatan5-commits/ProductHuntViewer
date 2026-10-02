package com.example.producthunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenScreen(
    tokenValue: String,
    onTokenChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isValidating: Boolean,
    errorMessage: String?
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Product Hunt Viewer",
            style = MaterialTheme.typography.titleLarge
        )
        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
        Text(
            text = "Cole seu developer token do Product Hunt para começar. " +
                "Ele fica salvo criptografado apenas neste aparelho.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))

        OutlinedTextField(
            value = tokenValue,
            onValueChange = onTokenChange,
            label = { Text("Developer token") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = errorMessage != null,
            supportingText = { errorMessage?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

        Button(
            onClick = onSubmit,
            enabled = !isValidating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isValidating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Entrar")
            }
        }

        TextButton(onClick = {
            uriHandler.openUri("https://www.producthunt.com/v2/oauth/applications")
        }) {
            Text("Onde encontro meu developer token?")
        }
    }
}
