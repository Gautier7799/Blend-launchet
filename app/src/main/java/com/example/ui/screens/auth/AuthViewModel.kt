package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthState())
    val uiState = _uiState.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        if (authRepository.currentUser != null) {
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) return
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun register() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) return
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun quickDemoLogin() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            email = "amjed.dev@google.com"
        )
    }

    fun logout() {
        authRepository.signOut()
        _uiState.value = AuthState()
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory = 
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
    }
}
