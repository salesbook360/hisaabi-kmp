package com.hisaabi.hisaabi_kmp.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard ViewModel to manage dashboard state and load data from database
 * Based on native Android app's DashboardUIViewModel
 */
class DashboardViewModel(
    private val repository: DashboardRepository,
    private val sessionManager: AppSessionManager
) : ViewModel() {
    
    // Get business slug from session manager
    private var businessSlug: String? = null
    
    // State for each dashboard section
    private val _balanceOverview = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val balanceOverview: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _balanceOverview.asStateFlow()
    
    private val _paymentOverview = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val paymentOverview: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _paymentOverview.asStateFlow()
    
    private val _salesOverview = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val salesOverview: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _salesOverview.asStateFlow()
    
    private val _purchaseOverview = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val purchaseOverview: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _purchaseOverview.asStateFlow()
    
    private val _inventorySummary = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val inventorySummary: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _inventorySummary.asStateFlow()
    
    private val _partiesSummary = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val partiesSummary: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _partiesSummary.asStateFlow()
    
    private val _productsSummary = MutableStateFlow<DashboardDataState<DashboardSectionDataModel>>(
        DashboardDataState.Loading
    )
    val productsSummary: StateFlow<DashboardDataState<DashboardSectionDataModel>> = _productsSummary.asStateFlow()
    
    // Selected intervals for filterable sections
    private var paymentInterval = IntervalEnum.THIS_MONTH
    private var salesInterval = IntervalEnum.THIS_MONTH
    private var purchaseInterval = IntervalEnum.THIS_MONTH
    
    init {
        // Observe business changes and reload dashboard
        viewModelScope.launch {
            sessionManager.observeBusinessSlug().collect { newBusinessSlug ->
                businessSlug = newBusinessSlug
                if (newBusinessSlug != null) {
                    loadDashboardData()
                }
            }
        }
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        loadBalanceOverview()
        loadPaymentOverview()
        loadSalesOverview()
        loadPurchaseOverview()
        loadInventorySummary()
        loadPartiesSummary()
        loadProductsSummary()
    }
    
    /**
     * Reload specific section when interval changes
     */
    fun onPaymentIntervalChanged(interval: IntervalEnum) {
        paymentInterval = interval
        loadPaymentOverview()
    }
    
    fun onSalesIntervalChanged(interval: IntervalEnum) {
        salesInterval = interval
        loadSalesOverview()
    }
    
    fun onPurchaseIntervalChanged(interval: IntervalEnum) {
        purchaseInterval = interval
        loadPurchaseOverview()
    }
    
    // Load Balance Overview
    private fun loadBalanceOverview() {
        viewModelScope.launch {
            _balanceOverview.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _balanceOverview.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getBalanceOverview(slug, IntervalEnum.ALL_RECORD)
                if (data.sectionItems.isEmpty()) {
                    _balanceOverview.value = DashboardDataState.NoData
                } else {
                    _balanceOverview.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _balanceOverview.value = DashboardDataState.Error(e.message ?: "Failed to load balance overview")
            }
        }
    }
    
    // Load Payment Overview
    private fun loadPaymentOverview() {
        viewModelScope.launch {
            _paymentOverview.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _paymentOverview.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getPaymentOverview(
                    slug,
                    paymentInterval
                ).copy(onOptionSelected = ::onPaymentIntervalChanged)
                
                if (data.sectionItems.isEmpty()) {
                    _paymentOverview.value = DashboardDataState.NoData
                } else {
                    _paymentOverview.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _paymentOverview.value = DashboardDataState.Error(e.message ?: "Failed to load payment overview")
            }
        }
    }
    
    // Load Sales Overview
    private fun loadSalesOverview() {
        viewModelScope.launch {
            _salesOverview.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _salesOverview.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getSalesOverview(
                    slug,
                    salesInterval
                ).copy(onOptionSelected = ::onSalesIntervalChanged)
                
                if (data.sectionItems.isEmpty()) {
                    _salesOverview.value = DashboardDataState.NoData
                } else {
                    _salesOverview.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _salesOverview.value = DashboardDataState.Error(e.message ?: "Failed to load sales overview")
            }
        }
    }
    
    // Load Purchase Overview
    private fun loadPurchaseOverview() {
        viewModelScope.launch {
            _purchaseOverview.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _purchaseOverview.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getPurchaseOverview(
                    slug,
                    purchaseInterval
                ).copy(onOptionSelected = ::onPurchaseIntervalChanged)
                
                if (data.sectionItems.isEmpty()) {
                    _purchaseOverview.value = DashboardDataState.NoData
                } else {
                    _purchaseOverview.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _purchaseOverview.value = DashboardDataState.Error(e.message ?: "Failed to load purchase overview")
            }
        }
    }
    
    // Load Inventory Summary
    private fun loadInventorySummary() {
        viewModelScope.launch {
            _inventorySummary.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _inventorySummary.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getInventorySummary(slug)
                if (data.sectionItems.isEmpty()) {
                    _inventorySummary.value = DashboardDataState.NoData
                } else {
                    _inventorySummary.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _inventorySummary.value = DashboardDataState.Error(e.message ?: "Failed to load inventory summary")
            }
        }
    }
    
    // Load Parties Summary
    private fun loadPartiesSummary() {
        viewModelScope.launch {
            _partiesSummary.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _partiesSummary.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getPartiesSummary(slug)
                if (data.sectionItems.isEmpty()) {
                    _partiesSummary.value = DashboardDataState.NoData
                } else {
                    _partiesSummary.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _partiesSummary.value = DashboardDataState.Error(e.message ?: "Failed to load parties summary")
            }
        }
    }
    
    // Load Products Summary
    private fun loadProductsSummary() {
        viewModelScope.launch {
            _productsSummary.value = DashboardDataState.Loading
            try {
                val slug = businessSlug
                if (slug == null) {
                    _productsSummary.value = DashboardDataState.Error("No business selected")
                    return@launch
                }
                val data = repository.getProductsSummary(slug)
                if (data.sectionItems.isEmpty()) {
                    _productsSummary.value = DashboardDataState.NoData
                } else {
                    _productsSummary.value = DashboardDataState.Success(data)
                }
            } catch (e: Exception) {
                _productsSummary.value = DashboardDataState.Error(e.message ?: "Failed to load products summary")
            }
        }
    }
    
    /**
     * Refresh all dashboard data
     */
    fun refresh() {
        loadDashboardData()
    }
}

