package com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.parties.data.repository.PartiesRepository
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.AddPartyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddPartyViewModel(
    private val addPartyUseCase: AddPartyUseCase,
    private val partiesRepository: PartiesRepository,
    private val categoryDao: CategoryDao,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    // Get slugs from session manager  
    private var businessSlug: String? = null
    private var userSlug: String? = null
    
    private val _uiState = MutableStateFlow(AddPartyUiState())
    val uiState: StateFlow<AddPartyUiState> = _uiState.asStateFlow()
    
    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()
    
    private val _areas = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val areas: StateFlow<List<CategoryEntity>> = _areas.asStateFlow()
    
    private var partyToEdit: Party? = null
    
    init {
        // Observe session context changes
        viewModelScope.launch {
            sessionManager.observeSessionContext().collect { context ->
                businessSlug = context.businessSlug
                userSlug = context.userSlug
                if (context.businessSlug != null) {
                    loadCategories()
                    loadAreas()
                }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AddPartyUiState()
        partyToEdit = null
        // Reload categories and areas
        loadCategories()
        loadAreas()
    }
    
    fun setPartyToEdit(party: Party) {
        partyToEdit = party
        // Reset UI state to clear any previous success/error states
        _uiState.value = AddPartyUiState()
        // Reload categories and areas
        loadCategories()
        loadAreas()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val slug = businessSlug ?: return@launch
                _categories.value = categoryDao.getCategoriesByTypeAndBusiness(CategoryType.CUSTOMER_CATEGORY.type,slug)
            } catch (e: Exception) {
                println("Error loading categories: ${e.message}")
            }
        }
    }
    
    private fun loadAreas() {
        viewModelScope.launch {
            try {
                val slug = businessSlug ?: return@launch
                _areas.value = categoryDao.getCategoriesByTypeAndBusiness(CategoryType.AREA.type, slug)
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
            
            val bSlug = businessSlug
            val uSlug = userSlug
            if (bSlug == null || uSlug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No business or user context available"
                )
                return@launch
            }
            
            val result = addPartyUseCase(
                name = name,
                phone = phone,
                address = address,
                email = email,
                description = description,
                openingBalance = openingBalance,
                isBalancePayable = isBalancePayable,
                partyType = partyType,
                businessSlug = bSlug,
                userSlug = uSlug,
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
    
    fun updateParty(
        party: Party,
        name: String,
        phone: String?,
        address: String?,
        email: String?,
        description: String?,
        openingBalance: Double,
        isBalancePayable: Boolean,
        categorySlug: String? = null,
        areaSlug: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Create lat/long string if both are provided
                val latLong = if (latitude != null && longitude != null) {
                    "$latitude,$longitude"
                } else null
                
                // Calculate balance adjustment based on isBalancePayable
                val adjustedBalance = if (isBalancePayable) openingBalance else -openingBalance
                
                // Create updated party
                val updatedParty = party.copy(
                    name = name,
                    phone = phone,
                    address = address,
                    email = email,
                    description = description,
                    openingBalance = adjustedBalance,
                    categorySlug = categorySlug,
                    areaSlug = areaSlug,
                    latLong = latLong
                )
                
                // Update party in repository
                partiesRepository.updateParty(updatedParty)
                
                println("Party updated successfully")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                println("Failed to update party: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = e.message ?: "Failed to update party"
                )
            }
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

