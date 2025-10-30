package com.hisaabi.hisaabi_kmp.parties.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartiesFilter
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.DeletePartyUseCase
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.GetPartiesCountUseCase
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.GetPartiesUseCase
import com.hisaabi.hisaabi_kmp.parties.domain.usecase.GetTotalBalanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PartiesViewModel(
    private val getPartiesUseCase: GetPartiesUseCase,
    private val getPartiesCountUseCase: GetPartiesCountUseCase,
    private val getTotalBalanceUseCase: GetTotalBalanceUseCase,
    private val deletePartyUseCase: DeletePartyUseCase,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    // Get business slug from session manager
    private var businessSlug: String? = null
    
    private val _uiState = MutableStateFlow(PartiesUiState())
    val uiState: StateFlow<PartiesUiState> = _uiState.asStateFlow()
    
    private val pageSize = 20
    private var currentPage = 0
    private var canLoadMore = true
    
    init {
        // Observe business changes and reload parties
        viewModelScope.launch {
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                businessSlug = newBusinessSlug
                if (newBusinessSlug != null) {
                    loadParties(reset = true)
                    loadTotalBalance()
                }
            }
        }
    }
    
    fun onSegmentChanged(segment: PartySegment) {
        if (_uiState.value.selectedSegment != segment) {
            _uiState.value = _uiState.value.copy(selectedSegment = segment)
            loadParties(reset = true)
            loadTotalBalance()
        }
    }
    
    fun onFilterChanged(filter: PartiesFilter) {
        if (_uiState.value.selectedFilter != filter) {
            _uiState.value = _uiState.value.copy(selectedFilter = filter)
            loadParties(reset = true)
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadParties(reset = true)
    }
    
    fun loadMoreParties() {
        if (!_uiState.value.isLoading && canLoadMore) {
            loadParties(reset = false)
        }
    }
    
    fun refresh() {
        loadParties(reset = true)
        loadTotalBalance()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun deleteParty(party: Party) {
        viewModelScope.launch {
            val result = deletePartyUseCase(party)
            result.fold(
                onSuccess = {
                    // Refresh the list after deletion
                    refresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete party"
                    )
                }
            )
        }
    }
    
    private fun loadParties(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                currentPage = 0
                canLoadMore = true
                _uiState.value = _uiState.value.copy(
                    parties = emptyList(),
                    isLoading = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }
            
            val slug = businessSlug
            if (slug == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "No business selected"
                )
                return@launch
            }
            
            val result = getPartiesUseCase(
                segment = _uiState.value.selectedSegment,
                filter = _uiState.value.selectedFilter,
                businessSlug = slug,
                searchQuery = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                pageSize = pageSize,
                pageNumber = currentPage
            )
            
            result.fold(
                onSuccess = { newParties ->
                    val allParties = if (reset) {
                        newParties
                    } else {
                        _uiState.value.parties + newParties
                    }
                    
                    canLoadMore = newParties.size == pageSize
                    currentPage++
                    
                    _uiState.value = _uiState.value.copy(
                        parties = allParties,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null
                    )
                    
                    // Load count on initial load
                    if (reset) {
                        loadPartiesCount()
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load parties"
                    )
                }
            )
        }
    }
    
    private fun loadPartiesCount() {
        viewModelScope.launch {
            val slug = businessSlug ?: return@launch
            
            val result = getPartiesCountUseCase(
                segment = _uiState.value.selectedSegment,
                businessSlug = slug,
                searchQuery = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            )
            
            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(totalCount = count)
            }
        }
    }
    
    private fun loadTotalBalance() {
        viewModelScope.launch {
            val slug = businessSlug ?: return@launch
            
            val result = getTotalBalanceUseCase(
                segment = _uiState.value.selectedSegment,
                businessSlug = slug
            )
            
            result.onSuccess { balance ->
                _uiState.value = _uiState.value.copy(totalBalance = balance)
            }
        }
    }
}

data class PartiesUiState(
    val parties: List<Party> = emptyList(),
    val selectedSegment: PartySegment = PartySegment.CUSTOMER,
    val selectedFilter: PartiesFilter = PartiesFilter.ALL_PARTIES,
    val searchQuery: String = "",
    val totalCount: Int = 0,
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null
)

