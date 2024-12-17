package com.zaed.barcodescanner.ui.main.components

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductsFolder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoldersList(
    modifier: Modifier = Modifier,
    folders: List<ProductsFolder>,
    onAddImageClicked: (folderName: String) -> Unit = {},
    onDeleteImage: (folderName: String, imageUri: Uri) -> Unit = { _, _ -> },
    onDeleteFolderClicked: (folderName: String) -> Unit = {},
) {
    AnimatedContent(targetState = folders.isEmpty(), label = "folder animated content") { state ->
        when {
            state -> {
                Text(
                    modifier = modifier.padding(top = 16.dp),
                    text = stringResource(R.string.no_folders_added_yet),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            else -> {
                LazyColumn(
                    modifier = modifier,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                    items(folders) { folder ->
                        FolderItem(
                            folder = folder,
                            onAddImageClicked = {
                                onAddImageClicked(folder.name)
                            },
                            onDeleteFolderClicked = {
                                onDeleteFolderClicked(folder.name)
                            },
                            onDeleteImage = { uri ->
                                onDeleteImage(folder.name, uri)
                            }
                        )
                    }
                }
            }
        }
    }

}

