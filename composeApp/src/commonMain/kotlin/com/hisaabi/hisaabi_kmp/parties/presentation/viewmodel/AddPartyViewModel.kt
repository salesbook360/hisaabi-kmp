package com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.AddPartyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddPartyViewModel(
    private val addPartyUseCase: AddPartyUseCase,
    private val categoryDao: CategoryDao
) : ViewModel() {
    
    // TODO: Get from session/business context
    private val businessSlug: String = "default_business"
    private val userSlug: String = "default_user"
    
    private val _uiState = MutableStateFlow(AddPartyUiState())
    val uiState: StateFlow<AddPartyUiState> = _uiState.asStateFlow()
    
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()
    
    private val _areas = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val areas: StateFlow<List<CategoryEntity>> = _areas.asStateFlow()
    
    init {
        loadCategories()
        loadAreas()
    }
    
    fun resetState() {
        _uiState.value = AddPartyUiState()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                // Type ID 3 = Customer Category (from CategoryTypeEnum)
                _categories.value = categoryDao.getCategoriesByTypeAndBusiness(3, businessSlug)
            } catch (e: Exception) {
                println("Error loading categories: ${e.message}")
            }
        }
    }
    
    private fun loadAreas() {
        viewModelScope.launch {
            try {
                // Type ID 2 = Area (from CategoryTypeEnum)
                _areas.value = categoryDao.getCategoriesByTypeAndBusiness(2, businessSlug)
            } catch (e: Exception) {
                println("Error loading areas: ${e.message}")
            }
        }
    }
    
    fun addParty(
        name: String,
        phone: String?,
        address: String?,
        email: String?,
        description: String?,
        openingBalance: Double,
        isBalancePayable: Boolean,
        partyType: PartyType,
        categorySlug: String? = null,
        areaSlug: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = addPartyUseCase(
                name = name,
                phone = phone,
                address = address,
                email = email,
                description = description,
                openingBalance = openingBalance,
                isBalancePayable = isBalancePayable,
                partyType = partyType,
                businessSlug = businessSlug,
                userSlug = userSlug,
                categorySlug = categorySlug,
                areaSlug = areaSlug,
                latitude = latitude,
                longitude = longitude
            )
            
            result.fold(
                onSuccess = { slug ->
                    println("Party added successfully with slug: $slug")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    println("Failed to add party: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = error.message ?: "Failed to add party"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AddPartyUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

