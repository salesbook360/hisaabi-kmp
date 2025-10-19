package com.hisaabi.hisaabi_kmp.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.profile.data.ProfileRepository
import com.hisaabi.hisaabi_kmp.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UpdateProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(UpdateProfileState())
    val state: StateFlow<UpdateProfileState> = _state.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            repository.getCurrentProfile()
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            profile = profile,
                            name = profile.name,
                            email = profile.email,
                            phone = profile.phone,
                            profilePicUrl = profile.pic,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load profile"
                        )
                    }
                }
        }
    }
    
    fun updateName(name: String) {
        _state.update { it.copy(name = name, error = null) }
    }
    
    fun updateEmail(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }
    
    fun updatePhone(phone: String) {
        _state.update { it.copy(phone = phone, error = null) }
    }
    
    fun updateProfilePicUrl(url: String) {
        _state.update { it.copy(profilePicUrl = url, error = null) }
    }
    
    fun saveProfile(authToken: String = "Bearer mock_token") {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Validation
            if (currentState.name.isBlank()) {
                _state.update { it.copy(error = "Name is required") }
                return@launch
            }
            
            if (currentState.email.isBlank()) {
                _state.update { it.copy(error = "Email is required") }
                return@launch
            }
            
            if (currentState.phone.isBlank()) {
                _state.update { it.copy(error = "Phone is required") }
                return@launch
            }
            
            // Email validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
                _state.update { it.copy(error = "Invalid email format") }
                return@launch
            }
            
            _state.update { it.copy(isSaving = true, error = null) }
            
            val updatedProfile = currentState.profile.copy(
                name = currentState.name.trim(),
                email = currentState.email.trim(),
                phone = currentState.phone.trim(),
                pic = currentState.profilePicUrl.trim()
            )
            
            repository.updateProfile(updatedProfile, authToken)
                .onSuccess { response ->
                    if (response.success) {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                isSaved = true,
                                profile = response.user ?: updatedProfile,
                                message = response.message.ifBlank { "Profile updated successfully" }
                            )
                        }
                        
                        // Reset saved flag after delay
                        kotlinx.coroutines.delay(2000)
                        _state.update { it.copy(isSaved = false, message = null) }
                    } else {
                        _state.update {
                            it.copy(
                                isSaving = false,
                                error = response.message.ifBlank { "Failed to update profile" }
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Failed to update profile"
                        )
                    }
                }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

data class UpdateProfileState(
    val profile: UserProfile = UserProfile.EMPTY,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePicUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

