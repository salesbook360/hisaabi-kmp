package com.hisaabi.hisaabi_kmp.reports.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportFilters
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportResult
import com.hisaabi.hisaabi_kmp.reports.domain.usecase.GenerateReportUseCase
import com.hisaabi.hisaabi_kmp.reports.domain.util.ReportPdfGenerator
import com.hisaabi.hisaabi_kmp.reports.domain.util.ShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val isLoading: Boolean = false,
    val reportResult: ReportResult? = null,
    val error: String? = null,
    val isGeneratingPdf: Boolean = false
)

class ReportViewModel(
    private val generateReportUseCase: GenerateReportUseCase,
    private val pdfGenerator: ReportPdfGenerator,
    private val shareHelper: ShareHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    fun generateReport(filters: ReportFilters) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )
                
                val result = generateReportUseCase.execute(filters) // Now suspend function
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reportResult = result
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to generate report"
                )
            }
        }
    }
    
    fun shareReportAsPdf() {
        val reportResult = _uiState.value.reportResult ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingPdf = true)
                
                val pdfPath = pdfGenerator.generatePdf(reportResult)
                
                _uiState.value = _uiState.value.copy(isGeneratingPdf = false)
                
                if (pdfPath != null) {
                    shareHelper.shareFile(
                        filePath = pdfPath,
                        mimeType = "application/pdf",
                        title = "Share ${reportResult.reportType.title}"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to generate PDF"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingPdf = false,
                    error = e.message ?: "Failed to share report"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearReport() {
        _uiState.value = ReportUiState()
    }
}

