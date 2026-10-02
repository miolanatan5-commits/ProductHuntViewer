package com.example.producthunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.producthunt.data.ProductHuntApi
import com.example.producthunt.data.TokenStore
import com.example.producthunt.ui.MainViewModel
import com.example.producthunt.ui.MainViewModelFactory
import com.example.producthunt.ui.screens.FilterSheet
import com.example.producthunt.ui.screens.PostsScreen
import com.example.producthunt.ui.screens.TokenScreen
import com.example.producthunt.ui.theme.ProductHuntTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProductHuntTheme {
                // A criação do TokenStore (Android Keystore/Tink) fica protegida
                // por try-catch: se falhar por qualquer motivo no aparelho, o app
                // mostra uma tela de erro com botão de tentar de novo em vez de
                // fechar sozinho sem explicação.
                var retryKey by remember { mutableIntStateOf(0) }
                val initResult = remember(retryKey) {
                    runCatching {
                        val store = TokenStore(applicationContext)
                        store to ProductHuntApi(store)
                    }
                }

                initResult.fold(
                    onSuccess = { (tokenStore, api) ->
                        val viewModel: MainViewModel = viewModel(
                            factory = MainViewModelFactory(tokenStore, api)
                        )
                        ProductHuntApp(viewModel)
                    },
                    onFailure = { error ->
                        InitErrorScreen(
                            message = error.message ?: error.javaClass.simpleName,
                            onRetry = { retryKey++ }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun InitErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Não foi possível iniciar o app", style = MaterialTheme.typography.titleLarge)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
        Text(
            "Houve um problema ao preparar o armazenamento seguro do token neste aparelho.\n\nDetalhe técnico: $message",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))
        Button(onClick = onRetry) {
            Text("Tentar novamente")
        }
    }
}

@Composable
fun ProductHuntApp(viewModel: MainViewModel) {
    var showFilters by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(viewModel.hasToken) {
        if (viewModel.hasToken && viewModel.posts.isEmpty() && !viewModel.isLoading) {
            viewModel.resetAndLoad()
        }
    }

    if (!viewModel.hasToken) {
        TokenScreen(
            tokenValue = viewModel.tokenFieldValue,
            onTokenChange = { viewModel.tokenFieldValue = it },
            onSubmit = { viewModel.submitToken() },
            isValidating = viewModel.validatingToken,
            errorMessage = viewModel.tokenError
        )
    } else {
        PostsScreen(
            posts = viewModel.posts,
            isLoading = viewModel.isLoading,
            errorMessage = viewModel.errorMessage,
            totalCount = viewModel.totalCount,
            pageNumber = viewModel.pageNumber,
            hasNextPage = viewModel.hasNextPage,
            hasPreviousPage = viewModel.hasPreviousPage,
            onNextPage = { viewModel.goToNextPage() },
            onPreviousPage = { viewModel.goToPreviousPage() },
            onOpenFilters = { showFilters = true },
            onSignOut = { viewModel.signOut() },
            onRefresh = { viewModel.resetAndLoad() }
        )

        if (showFilters) {
            FilterSheet(
                currentFilters = viewModel.filters,
                availableTopics = viewModel.availableTopics,
                loadingTopics = viewModel.loadingTopics,
                onSearchTopics = { viewModel.searchTopics(it) },
                onApply = {
                    viewModel.applyFilters(it)
                    showFilters = false
                },
                onDismiss = { showFilters = false }
            )
        }
    }
}
