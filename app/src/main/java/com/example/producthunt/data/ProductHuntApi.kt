package com.example.producthunt.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val isAuthError: Boolean = false) : ApiResult<Nothing>()
}

/**
 * Cliente simples para a API GraphQL v2 do Product Hunt.
 * Docs: https://api.producthunt.com/v2/docs
 */
class ProductHuntApi(private val tokenStore: TokenStore) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    private fun graphqlRequest(query: String): ApiResult<JSONObject> {
        val token = tokenStore.getToken()
            ?: return ApiResult.Error("Nenhum token configurado.", isAuthError = true)

        val body = JSONObject().put("query", query).toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (response.code == 401 || response.code == 403) {
                    return ApiResult.Error("Token inválido ou sem permissão.", isAuthError = true)
                }
                if (!response.isSuccessful) {
                    return ApiResult.Error("Erro HTTP ${response.code}")
                }
                val json = JSONObject(bodyStr)
                if (json.has("errors")) {
                    val errArray = json.getJSONArray("errors")
                    val msg = if (errArray.length() > 0) {
                        errArray.getJSONObject(0).optString("message", "Erro desconhecido")
                    } else "Erro desconhecido"
                    val isAuth = msg.contains("auth", ignoreCase = true) ||
                        msg.contains("token", ignoreCase = true)
                    return ApiResult.Error(msg, isAuthError = isAuth)
                }
                ApiResult.Success(json.getJSONObject("data"))
            }
        } catch (e: IOException) {
            ApiResult.Error("Falha de conexão: ${e.message ?: "verifique sua internet"}")
        } catch (e: Exception) {
            ApiResult.Error("Erro inesperado: ${e.message}")
        }
    }

    fun fetchPosts(filters: PostFilters, after: String?, pageSize: Int = 20): ApiResult<PostsPage> {
        val args = mutableListOf("first: $pageSize", "order: ${filters.order.apiValue}")
        after?.let { args.add("after: \"${escape(it)}\"") }
        filters.topicSlug?.takeIf { it.isNotBlank() }?.let { args.add("topic: \"${escape(it)}\"") }
        filters.postedAfter?.takeIf { it.isNotBlank() }?.let { args.add("postedAfter: \"${escape(it)}\"") }
        filters.postedBefore?.takeIf { it.isNotBlank() }?.let { args.add("postedBefore: \"${escape(it)}\"") }
        filters.url?.takeIf { it.isNotBlank() }?.let { args.add("url: \"${escape(it)}\"") }
        filters.twitterUrl?.takeIf { it.isNotBlank() }?.let { args.add("twitterUrl: \"${escape(it)}\"") }
        when (filters.featured) {
            FeaturedFilter.FEATURED_ONLY -> args.add("featured: true")
            FeaturedFilter.NOT_FEATURED -> args.add("featured: false")
            FeaturedFilter.ANY -> {}
        }

        val query = """
            query {
              posts(${args.joinToString(", ")}) {
                totalCount
                pageInfo { hasNextPage endCursor }
                edges {
                  node {
                    id
                    name
                    tagline
                    description
                    votesCount
                    commentsCount
                    url
                    website
                    createdAt
                    featuredAt
                    thumbnail { url }
                    topics(first: 5) { edges { node { name } } }
                    makers { name }
                  }
                }
              }
            }
        """.trimIndent()

        return when (val result = graphqlRequest(query)) {
            is ApiResult.Error -> result
            is ApiResult.Success -> {
                try {
                    val postsObj = result.data.getJSONObject("posts")
                    val pageInfo = postsObj.getJSONObject("pageInfo")
                    val edges = postsObj.getJSONArray("edges")
                    val posts = (0 until edges.length()).map { i ->
                        parsePost(edges.getJSONObject(i).getJSONObject("node"))
                    }.distinctBy { it.id } // a API pode repetir um post entre paginas quando o
                    // ranking muda no meio da sessao; ids repetidos derrubam o LazyColumn
                    // (Compose exige key unica), o que fazia a pagina "perder" itens de repente.
                    ApiResult.Success(
                        PostsPage(
                            posts = posts,
                            endCursor = pageInfo.optString("endCursor").takeIf { it.isNotBlank() },
                            hasNextPage = pageInfo.optBoolean("hasNextPage", false),
                            totalCount = postsObj.optInt("totalCount", posts.size)
                        )
                    )
                } catch (e: Exception) {
                    ApiResult.Error("Erro ao interpretar resposta: ${e.message}")
                }
            }
        }
    }

    fun fetchTopics(search: String?): ApiResult<List<Topic>> {
        val args = mutableListOf("first: 30", "order: FOLLOWERS_COUNT")
        search?.takeIf { it.isNotBlank() }?.let { args.add("query: \"${escape(it)}\"") }

        val query = """
            query {
              topics(${args.joinToString(", ")}) {
                edges { node { id name slug } }
              }
            }
        """.trimIndent()

        return when (val result = graphqlRequest(query)) {
            is ApiResult.Error -> result
            is ApiResult.Success -> {
                try {
                    val edges = result.data.getJSONObject("topics").getJSONArray("edges")
                    val topics = (0 until edges.length()).map { i ->
                        val node = edges.getJSONObject(i).getJSONObject("node")
                        Topic(
                            id = node.getString("id"),
                            name = node.getString("name"),
                            slug = node.getString("slug")
                        )
                    }
                    ApiResult.Success(topics)
                } catch (e: Exception) {
                    ApiResult.Error("Erro ao interpretar tópicos: ${e.message}")
                }
            }
        }
    }

    /** Valida o token fazendo uma chamada leve (viewer). */
    fun validateToken(token: String): ApiResult<Boolean> {
        val body = JSONObject().put("query", "query { viewer { user { id name } } }")
            .toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                when {
                    response.code == 401 || response.code == 403 ->
                        ApiResult.Error("Token inválido.", isAuthError = true)
                    !response.isSuccessful -> ApiResult.Error("Erro HTTP ${response.code}")
                    else -> ApiResult.Success(true)
                }
            }
        } catch (e: IOException) {
            ApiResult.Error("Falha de conexão: ${e.message ?: "verifique sua internet"}")
        }
    }

    private fun parsePost(node: JSONObject): Post {
        val topicsEdges: JSONArray = node.optJSONObject("topics")?.optJSONArray("edges") ?: JSONArray()
        val topics = (0 until topicsEdges.length()).map {
            topicsEdges.getJSONObject(it).getJSONObject("node").getString("name")
        }
        val makersArr = node.optJSONArray("makers") ?: JSONArray()
        val makers = (0 until makersArr.length()).map { makersArr.getJSONObject(it).optString("name") }

        return Post(
            id = node.getString("id"),
            name = node.optString("name"),
            tagline = node.optString("tagline"),
            description = node.optString("description"),
            votesCount = node.optInt("votesCount"),
            commentsCount = node.optInt("commentsCount"),
            url = node.optString("url"),
            website = node.optString("website").takeIf { it.isNotBlank() },
            createdAt = node.optString("createdAt"),
            featuredAt = node.optString("featuredAt").takeIf { it.isNotBlank() },
            thumbnailUrl = node.optJSONObject("thumbnail")?.optString("url"),
            topics = topics,
            makers = makers
        )
    }

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val ENDPOINT = "https://api.producthunt.com/v2/api/graphql"
    }
}
