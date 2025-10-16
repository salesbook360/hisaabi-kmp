package com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.categories.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {
    
    // TODO: Get from session/business context
    private val businessSlug: String = "default_business"
    
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    fun loadCategories(categoryType: CategoryType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = getCategoriesUseCase(categoryType, businessSlug)
            
            result.fold(
                onSuccess = { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        categories = emptyList(),
                        isLoading = false,
                        error = error.message ?: "Failed to load categories"
                    )
                }
            )
        }
    }
}

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

