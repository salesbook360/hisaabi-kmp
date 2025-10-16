package com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.categories.domain.usecase.AddCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModel() {
    
    // TODO: Get from session/business context
    private val businessSlug: String = "default_business"
    private val userSlug: String = "default_user"
    
    private val _uiState = MutableStateFlow(AddCategoryUiState())
    val uiState: StateFlow<AddCategoryUiState> = _uiState.asStateFlow()
    
    fun addCategory(
        title: String,
        description: String?,
        categoryType: CategoryType
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = addCategoryUseCase(
                title = title,
                description = description,
                categoryType = categoryType,
                businessSlug = businessSlug,
                userSlug = userSlug
            )
            
            result.fold(
                onSuccess = { slug ->
                    println("Category added successfully with slug: $slug")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    println("Failed to add category: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = error.message ?: "Failed to add category"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AddCategoryUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

