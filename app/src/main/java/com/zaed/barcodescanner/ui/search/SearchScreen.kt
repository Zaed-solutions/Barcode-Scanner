package com.zaed.barcodescanner.ui.search

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.SendAndArchive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductsFolder
import com.zaed.barcodescanner.ui.components.EmptySearchResult
import com.zaed.barcodescanner.ui.components.StatefulAsyncImage
import com.zaed.barcodescanner.ui.main.ZoomableImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    navigateToLogin: () -> Unit = {},
    navigateBack: () -> Unit = {}

) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreenContent(
        isSearching = state.isSearching,
        images = state.images,
        searchQuery = state.searchQuery,
        action = {
            when (it) {
                SearchUiAction.OnBackClicked -> navigateBack()
                else -> viewModel.handleAction(it)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    searchQuery: String = "",
    isSearching: Boolean,
    images: List<String>,
    action: (SearchUiAction) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Search"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            action(SearchUiAction.OnBackClicked)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            var isFullScreenImageVisible by remember {
                mutableStateOf(false to Uri.EMPTY)
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    action(SearchUiAction.OnSearchQueryChanged(it))
                },
                label = {
                    Text(
                        text = "Search"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ManageSearch,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            action(SearchUiAction.OnSearchClicked)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                }
            )
            AnimatedContent(searchQuery.isBlank() to images.isEmpty()) { state ->
                when {
                    state.first -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.loading_animation2
                                )
                            )
                            LottieAnimation(
                                modifier = Modifier
                                    .size(300.dp)
                                    .align(Alignment.Center),
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                            )
                        }
                    }

                    state.second && isSearching -> {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.loading_animation
                                )
                            )
                            LottieAnimation(
                                modifier = Modifier
                                    .size(300.dp)
                                    .align(Alignment.Center),
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                            )
                        }

                    }

                    state.second  -> {
                        EmptySearchResult()
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Log.d("SearchScreen", "SearchScreenContent: $images")
                            items(images) { uri ->
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clickable {
                                            isFullScreenImageVisible = true to Uri.parse(uri)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    StatefulAsyncImage(
                                        imageUrl = uri,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(isFullScreenImageVisible.first) {
                ModalBottomSheet(
                    dragHandle = {},
                    onDismissRequest = {
                        isFullScreenImageVisible = false to Uri.EMPTY
                    },
                    shape = RoundedCornerShape(0.dp),
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    ZoomableImage(
                        imageLink = isFullScreenImageVisible.second.toString(),
                        closePreview = {
                            isFullScreenImageVisible = false to Uri.EMPTY
                        }
                    )
                }
            }

        }
    }

}


@Preview
@Composable
fun SearchScreenPreview() {
    SearchScreenContent(
        isSearching = false,
        images = emptyList(),
        action = {}
    )
}


