package com.example.producthunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.producthunt.data.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    posts: List<Post>,
    isLoading: Boolean,
    errorMessage: String?,
    totalCount: Int,
    pageNumber: Int,
    hasNextPage: Boolean,
    hasPreviousPage: Boolean,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onOpenFilters: () -> Unit,
    onSignOut: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Product Hunt")
                        // Sempre mostra uma contagem: se a API não trouxer o total
                        // (campo totalCount às vezes vem 0 nela mesmo com resultados),
                        // cai pro tanto que já temos nesta página.
                        val countText = if (totalCount > 0) {
                            "$totalCount lançamentos"
                        } else if (posts.isNotEmpty()) {
                            "${posts.size} nesta página"
                        } else null
                        if (countText != null) {
                            Text(
                                countText,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenFilters) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtros")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sair / trocar token")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousPage, enabled = hasPreviousPage && !isLoading) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Página anterior")
                    }
                    Text("Página $pageNumber", style = MaterialTheme.typography.labelMedium)
                    IconButton(onClick = onNextPage, enabled = hasNextPage && !isLoading) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Próxima página")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && posts.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null && posts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        androidx.compose.material3.TextButton(onClick = onRefresh) {
                            Text("Tentar novamente")
                        }
                    }
                }
                posts.isEmpty() -> {
                    Text(
                        "Nenhum lançamento encontrado com esses filtros.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostCard(post = post, onOpen = { uriHandler.openUri(post.url) })
                        }
                    }
                }
            }

            if (isLoading && posts.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: Post, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (post.thumbnailUrl != null) {
                AsyncImage(
                    model = post.thumbnailUrl,
                    contentDescription = post.name,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 12.dp)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(post.name, style = MaterialTheme.typography.titleLarge)
                Text(post.tagline, style = MaterialTheme.typography.bodyLarge)

                if (post.topics.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        post.topics.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = "Votos",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(" ${post.votesCount}  ")
                    Icon(
                        Icons.Filled.ChatBubbleOutline,
                        contentDescription = "Comentários",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(" ${post.commentsCount}  ")
                    Icon(
                        Icons.Filled.OpenInBrowser,
                        contentDescription = "Abrir",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
