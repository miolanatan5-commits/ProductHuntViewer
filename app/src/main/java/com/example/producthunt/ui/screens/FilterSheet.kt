package com.example.producthunt.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.producthunt.data.DateUtils
import com.example.producthunt.data.FeaturedFilter
import com.example.producthunt.data.PlatformFilter
import com.example.producthunt.data.PostFilters
import com.example.producthunt.data.PostsOrder
import com.example.producthunt.data.Topic

/**
 * Painel com todos os filtros suportados pela query `posts` da API v2:
 * order, featured, topic, postedAfter, postedBefore, url, twitterUrl —
 * mais o seletor de plataforma (que resolve pro tópico certo via busca na
 * própria API) e o interruptor "somente hoje" (calcula o intervalo do dia
 * no fuso do aparelho e converte pro formato ISO 8601/UTC que a API espera).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    currentFilters: PostFilters,
    availableTopics: List<Topic>,
    loadingTopics: Boolean,
    onSearchTopics: (String) -> Unit,
    onApply: (PostFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var order by remember { mutableStateOf(currentFilters.order) }
    var featured by remember { mutableStateOf(currentFilters.featured) }
    var topicSlug by remember { mutableStateOf(currentFilters.topicSlug.orEmpty()) }
    var postedAfter by remember { mutableStateOf(currentFilters.postedAfter.orEmpty()) }
    var postedBefore by remember { mutableStateOf(currentFilters.postedBefore.orEmpty()) }
    var url by remember { mutableStateOf(currentFilters.url.orEmpty()) }
    var twitterUrl by remember { mutableStateOf(currentFilters.twitterUrl.orEmpty()) }
    var showAdvanced by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf(currentFilters.platform) }
    var onlyToday by remember { mutableStateOf(currentFilters.onlyToday) }

    LaunchedEffect(topicSlug) {
        if (topicSlug.length >= 2) onSearchTopics(topicSlug)
    }

    // Resolve dinamicamente o slug do tópico da plataforma escolhida, buscando
    // na própria API (evita depender de um slug fixo que pode mudar).
    LaunchedEffect(selectedPlatform, availableTopics) {
        val platform = selectedPlatform
        if (platform != null) {
            val match = availableTopics.firstOrNull {
                it.slug.contains(platform.searchTerm.replace(" ", "-"), ignoreCase = true) ||
                    it.name.contains(platform.label, ignoreCase = true)
            }
            topicSlug = match?.slug ?: platform.fallbackSlug
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Text("Plataforma", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlatformFilter.entries.forEach { option ->
                    FilterChip(
                        selected = selectedPlatform == option,
                        onClick = {
                            if (selectedPlatform == option) {
                                // toca de novo pra desmarcar
                                selectedPlatform = null
                                topicSlug = ""
                            } else {
                                selectedPlatform = option
                                onSearchTopics(option.searchTerm)
                            }
                        },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Somente hoje", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "Mostra só os lançamentos do dia atual do aparelho",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Switch(
                    checked = onlyToday,
                    onCheckedChange = { checked ->
                        onlyToday = checked
                        if (checked) {
                            val (start, end) = DateUtils.todayRangeIso()
                            postedAfter = start
                            postedBefore = end
                        } else {
                            postedAfter = ""
                            postedBefore = ""
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Ordenar por", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostsOrder.entries.forEach { option ->
                    FilterChip(
                        selected = order == option,
                        onClick = { order = option },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Destaque", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeaturedFilter.entries.forEach { option ->
                    FilterChip(
                        selected = featured == option,
                        onClick = { featured = option },
                        label = { Text(option.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Tópico", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = topicSlug,
                onValueChange = {
                    topicSlug = it
                    selectedPlatform = null // digitar manualmente desmarca o chip de plataforma
                },
                label = { Text("Nome ou slug do tópico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (availableTopics.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(availableTopics) { topic ->
                        SuggestionChip(
                            onClick = {
                                topicSlug = topic.slug
                                selectedPlatform = null
                            },
                            label = { Text(topic.name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Ocultar filtros avançados" else "Mostrar filtros avançados (datas, URL)")
            }

            if (showAdvanced) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (onlyToday) {
                        "Datas calculadas automaticamente pelo interruptor \"Somente hoje\" acima."
                    } else {
                        "Datas em formato ISO 8601, ex: 2026-08-01T00:00:00Z"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = postedAfter,
                    onValueChange = { postedAfter = it },
                    label = { Text("Publicado após (postedAfter)") },
                    singleLine = true,
                    enabled = !onlyToday,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = postedBefore,
                    onValueChange = { postedBefore = it },
                    label = { Text("Publicado antes (postedBefore)") },
                    singleLine = true,
                    enabled = !onlyToday,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL exata do produto (url)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = twitterUrl,
                    onValueChange = { twitterUrl = it },
                    label = { Text("URL do Twitter/X (twitterUrl)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        order = PostsOrder.RANKING
                        featured = FeaturedFilter.ANY
                        topicSlug = ""
                        postedAfter = ""
                        postedBefore = ""
                        url = ""
                        twitterUrl = ""
                        selectedPlatform = null
                        onlyToday = false
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Limpar") }

                Button(
                    onClick = {
                        onApply(
                            PostFilters(
                                order = order,
                                featured = featured,
                                topicSlug = topicSlug.trim().ifBlank { null },
                                postedAfter = postedAfter.trim().ifBlank { null },
                                postedBefore = postedBefore.trim().ifBlank { null },
                                url = url.trim().ifBlank { null },
                                twitterUrl = twitterUrl.trim().ifBlank { null },
                                platform = selectedPlatform,
                                onlyToday = onlyToday
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Aplicar") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
