package com.example.producthunt.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.producthunt.data.ApiResult
import com.example.producthunt.data.PostFilters
import com.example.producthunt.data.Post
import com.example.producthunt.data.PostsPage
import com.example.producthunt.data.ProductHuntApi
import com.example.producthunt.data.TokenStore
import com.example.producthunt.data.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val tokenStore: TokenStore,
    private val api: ProductHuntApi
) : ViewModel() {

    var hasToken by mutableStateOf(tokenStore.hasToken())
        private set

    var tokenFieldValue by mutableStateOf("")
    var tokenError by mutableStateOf<String?>(null)
        private set
    var validatingToken by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var totalCount by mutableStateOf(0)
        private set

    var filters by mutableStateOf(PostFilters())
        private set

    var availableTopics by mutableStateOf<List<Topic>>(emptyList())
        private set
    var loadingTopics by mutableStateOf(false)
        private set

    // --- Paginacao por paginas: cada pagina e cacheada ao ser buscada,
    // entao "Anterior" nunca refaz chamada de rede, so troca o indice. ---
    private val pageCache = mutableListOf<PostsPage>()
    var currentPageIndex by mutableStateOf(0)
        private set

    val posts: List<Post>
        get() = pageCache.getOrNull(currentPageIndex)?.posts.orEmpty()

    val hasNextPage: Boolean
        get() = pageCache.getOrNull(currentPageIndex)?.hasNextPage ?: false

    val hasPreviousPage: Boolean
        get() = currentPageIndex > 0

    val pageNumber: Int
        get() = currentPageIndex + 1

    fun submitToken() {
        val token = tokenFieldValue.trim()
        if (token.isBlank()) {
            tokenError = "Informe o developer token."
            return
        }
        validatingToken = true
        tokenError = null
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { api.validateToken(token) }
            validatingToken = false
            when (result) {
                is ApiResult.Success -> {
                    tokenStore.saveToken(token)
                    hasToken = true
                    tokenFieldValue = ""
                    resetAndLoad()
                }
                is ApiResult.Error -> tokenError = result.message
            }
        }
    }

    fun signOut() {
        tokenStore.clearToken()
        hasToken = false
        pageCache.clear()
        currentPageIndex = 0
    }

    // Contador de "geracao" da paginacao: toda vez que resetAndLoad() roda (troca de
    // filtro, primeiro carregamento), incrementa. Uma resposta de rede que chega depois
    // de um reset (ex: usuario trocou o filtro enquanto a pagina anterior ainda estava
    // carregando) e da geracao antiga e descartada, em vez de ser gravada por cima do
    // cache ja limpo - isso que causava a pagina mostrar so 1 item ou o total errado.
    private var requestGeneration = 0

    /** Reinicia a paginacao do zero (usado ao aplicar filtros ou no primeiro carregamento). */
    fun resetAndLoad() {
        requestGeneration++
        pageCache.clear()
        currentPageIndex = 0
        errorMessage = null
        fetchPage(afterCursor = null, targetIndex = 0, generation = requestGeneration)
    }

    fun goToNextPage() {
        if (!hasNextPage || isLoading) return
        val nextIndex = currentPageIndex + 1
        if (nextIndex < pageCache.size) {
            // ja buscada antes (ex: usuario voltou e avancou de novo) - sem chamada de rede
            currentPageIndex = nextIndex
            return
        }
        val cursor = pageCache.getOrNull(currentPageIndex)?.endCursor
        fetchPage(afterCursor = cursor, targetIndex = nextIndex, generation = requestGeneration)
    }

    fun goToPreviousPage() {
        if (!hasPreviousPage || isLoading) return
        currentPageIndex -= 1
    }

    private fun fetchPage(afterCursor: String?, targetIndex: Int, generation: Int) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                api.fetchPosts(filters, afterCursor)
            }
            // Se um novo reset (troca de filtro) aconteceu enquanto essa chamada
            // estava em voo, essa resposta e antiga: ignora e nao mexe no cache atual.
            if (generation != requestGeneration) return@launch
            isLoading = false
            when (result) {
                is ApiResult.Success -> {
                    val page = result.data
                    if (targetIndex < pageCache.size) {
                        pageCache[targetIndex] = page
                    } else {
                        pageCache.add(page)
                    }
                    currentPageIndex = targetIndex
                    totalCount = page.totalCount
                }
                is ApiResult.Error -> {
                    errorMessage = result.message
                    if (result.isAuthError) signOut()
                }
            }
        }
    }

    fun applyFilters(newFilters: PostFilters) {
        filters = newFilters
        resetAndLoad()
    }

    fun searchTopics(query: String) {
        loadingTopics = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { api.fetchTopics(query) }
            loadingTopics = false
            if (result is ApiResult.Success) {
                availableTopics = result.data
            }
        }
    }
}
