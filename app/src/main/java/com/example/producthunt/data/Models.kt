package com.example.producthunt.data

data class Post(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val votesCount: Int,
    val commentsCount: Int,
    val url: String,
    val website: String?,
    val createdAt: String,
    val featuredAt: String?,
    val thumbnailUrl: String?,
    val topics: List<String>,
    val makers: List<String>
)

data class Topic(
    val id: String,
    val name: String,
    val slug: String
)

data class PostsPage(
    val posts: List<Post>,
    val endCursor: String?,
    val hasNextPage: Boolean,
    val totalCount: Int
)

/** Ordenação suportada pela API (enum PostsOrder). */
enum class PostsOrder(val apiValue: String, val label: String) {
    RANKING("RANKING", "Ranking"),
    NEWEST("NEWEST", "Mais recentes"),
    VOTES("VOTES", "Mais votados"),
    FEATURED_AT("FEATURED_AT", "Data de destaque")
}

/** Filtro de destaque (argumento booleano "featured" da API). */
enum class FeaturedFilter(val label: String) {
    ANY("Todos"),
    FEATURED_ONLY("Somente destaque"),
    NOT_FEATURED("Não destacados")
}

/**
 * Seletor de plataforma exibido nos filtros. A API do Product Hunt não tem um
 * argumento nativo "platform" na query `posts` — a forma real de filtrar por
 * plataforma é via o argumento `topic`, usando o tópico correspondente
 * (ex: o tópico "Android" agrupa os lançamentos daquela plataforma).
 * `fallbackSlug` é usado como valor conhecido; `searchTerm` é usado para
 * resolver dinamicamente o slug certo pela busca de tópicos da própria API,
 * garantindo que funcione mesmo se o Product Hunt mudar os slugs.
 */
enum class PlatformFilter(val label: String, val searchTerm: String, val fallbackSlug: String) {
    ANDROID("Android", "android", "android"),
    IOS("iOS", "ios", "ios"),
    DESKTOP("Desktop", "desktop", "desktop-app"),
    WEB("Web", "web app", "web-app")
}

/** Conjunto de filtros aceitos pela query `posts` da API do Product Hunt. */
data class PostFilters(
    val order: PostsOrder = PostsOrder.RANKING,
    val featured: FeaturedFilter = FeaturedFilter.ANY,
    val topicSlug: String? = null,
    val postedAfter: String? = null,   // ISO 8601 DateTime
    val postedBefore: String? = null,  // ISO 8601 DateTime
    val url: String? = null,
    val twitterUrl: String? = null,
    val platform: PlatformFilter? = null,  // guardado só pra manter o chip selecionado ao reabrir o painel
    val onlyToday: Boolean = false          // guardado só pra manter o interruptor ligado ao reabrir o painel
)
