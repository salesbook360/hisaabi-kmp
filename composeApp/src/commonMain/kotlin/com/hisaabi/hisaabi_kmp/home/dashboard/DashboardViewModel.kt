package com.hisaabi.hisaabi_kmp.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard ViewModel to manage dashboard state and load data from database
 * Based on native Android app's DashboardUIViewModel
 */
class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {
    
    // Default business slug - in real app, get from user preferences
    private val businessSlug = "default_business"
    
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
        loadDashboardData()
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
                val data = repository.getBalanceOverview(businessSlug, IntervalEnum.ALL_RECORD)
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
                val data = repository.getPaymentOverview(
                    businessSlug,
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
                val data = repository.getSalesOverview(
                    businessSlug,
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
                val data = repository.getPurchaseOverview(
                    businessSlug,
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
                val data = repository.getInventorySummary(businessSlug)
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
                val data = repository.getPartiesSummary(businessSlug)
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
                val data = repository.getProductsSummary(businessSlug)
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

