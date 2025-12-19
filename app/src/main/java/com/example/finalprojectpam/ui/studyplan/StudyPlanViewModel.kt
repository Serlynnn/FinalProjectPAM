package com.example.finalprojectpam.ui.studyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.StudyPlan
import com.example.finalprojectpam.data.repository.StudyPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyPlanUiState(
    val studyPlans: List<StudyPlan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StudyPlanViewModel(private val repository: StudyPlanRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyPlanUiState())
    val uiState: StateFlow<StudyPlanUiState> = _uiState.asStateFlow()

    init {
        loadStudyPlans()
    }

    fun loadStudyPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repository.getStudyPlans()
                _uiState.update { it.copy(studyPlans = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal memuat data") }
            }
        }
    }

    fun addStudyPlan(title: String, date: String) {
        viewModelScope.launch {
            if (title.isBlank() || date.isBlank()) return@launch
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.addStudyPlan(title, date)
                loadStudyPlans()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateStudyPlan(id: String, title: String, date: String) {
        viewModelScope.launch {
            try {
                repository.updateStudyPlan(id, title, date)
                loadStudyPlans()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteStudyPlan(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteStudyPlan(id)
                loadStudyPlans()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(private val repository: StudyPlanRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudyPlanViewModel(repository) as T
        }
    }
}