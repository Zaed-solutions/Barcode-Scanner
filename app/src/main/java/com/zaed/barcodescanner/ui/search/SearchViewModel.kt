package com.zaed.barcodescanner.ui.search

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaed.barcodescanner.data.models.ProductImage
import com.zaed.barcodescanner.data.models.ProductsFolder
import com.zaed.barcodescanner.data.source.remote.DriveRemoteSource
import com.zaed.barcodescanner.data.source.remote.GoogleAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val driveRemoteSource: DriveRemoteSource,
    private val googleAuth: GoogleAuth
):ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState= _uiState.asStateFlow()

    fun handleAction(action: SearchUiAction){
        when(action){
            SearchUiAction.OnSearchClicked -> searchFolderAndGetImages()
            is SearchUiAction.OnSearchQueryChanged -> updateSearchQuery(action.query)
            else -> {}
        }
    }

    private fun searchFolderAndGetImages() {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.IO) {
                googleAuth.getSignedInAccount().collect { result ->
                    result.onSuccess { account ->
                        _uiState.update { it.copy(needToLogin = false,isSearching = true) }
                        driveRemoteSource.searchFolderAndGetImages(account,uiState.value.searchQuery).collect{result->
                            result.onSuccess {data->
                                _uiState.update {it.copy(images = data,isSearching = false,errorMessage = "")}
                                Log.d("SearchViewModel", "handleAction: $data")
                            }.onFailure {
                                _uiState.update {it.copy(isSearching = false,errorMessage = it.errorMessage)}
                            }
                        }
                    }.onFailure {
                        _uiState.update { it.copy(needToLogin = true) }
                    }
                }
            }


        }
    }


    private fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query
            )
        }
    }
}

sealed interface SearchUiAction {
    data object OnSearchClicked : SearchUiAction
    data class OnSearchQueryChanged(val query: String) : SearchUiAction
    data object OnBackClicked : SearchUiAction
}

data class SearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val images: List<String> = emptyList(),
    val needToLogin: Boolean = false,
    val errorMessage: String = ""
)


