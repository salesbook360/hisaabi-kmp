package com.hisaabi.hisaabi_kmp.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import com.hisaabi.hisaabi_kmp.auth.domain.model.User
import com.hisaabi.hisaabi_kmp.auth.domain.model.onError
import com.hisaabi.hisaabi_kmp.auth.domain.model.onSuccess
import com.hisaabi.hisaabi_kmp.auth.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        checkAuthState()
        observeAuthState()
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        isLoggedIn = true
                    )
                    _isLoggedIn.value = true
                }
                .onError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
        }
    }
    
    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            registerUseCase(email, password, firstName, lastName)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        isLoggedIn = true
                    )
                    _isLoggedIn.value = true
                }
                .onError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            logoutUseCase()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = null,
                        isLoggedIn = false
                    )
                    _isLoggedIn.value = false
                }
                .onError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            val isLoggedIn = isLoggedInUseCase()
            val currentUser = if (isLoggedIn) getCurrentUserUseCase() else null
            
            _uiState.value = _uiState.value.copy(
                isLoggedIn = isLoggedIn,
                currentUser = currentUser
            )
            _isLoggedIn.value = isLoggedIn
        }
    }
    
    private fun observeAuthState() {
        viewModelScope.launch {
            isLoggedInUseCase.observe().collect { isLoggedIn ->
                _isLoggedIn.value = isLoggedIn
                _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
                
                if (!isLoggedIn) {
                    _uiState.value = _uiState.value.copy(currentUser = null)
                }
            }
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)
