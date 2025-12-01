package com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.categories.domain.usecase.GetCategoriesUseCase
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val sessionManager: AppSessionManager,

) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    private var businessSlug: String? = null
    
    init {
        viewModelScope.launch {
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                businessSlug = newBusinessSlug
                // Reload categories when business changes
                if (_uiState.value.selectedCategoryType != null) {
                    loadCategories(_uiState.value.selectedCategoryType!!)
                }
            }
        }
    }
    
    fun onCategoryTypeChanged(categoryType: CategoryType) {
        _uiState.value = _uiState.value.copy(selectedCategoryType = categoryType)
        loadCategories(categoryType)
    }
    
    fun loadCategories(categoryType: CategoryType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val slug = businessSlug
            if (slug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No business selected"
                )
                return@launch
            }
            
            val result = getCategoriesUseCase(categoryType, slug)
            
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
    val error: String? = null,
    val selectedCategoryType: CategoryType? = null
)



