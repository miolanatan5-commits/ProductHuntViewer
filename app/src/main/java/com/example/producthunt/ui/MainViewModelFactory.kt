package com.example.producthunt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.producthunt.data.ProductHuntApi
import com.example.producthunt.data.TokenStore

class MainViewModelFactory(
    private val tokenStore: TokenStore,
    private val api: ProductHuntApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(tokenStore, api) as T
    }
}
