package com.zaed.barcodescanner.ui.main.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaed.barcodescanner.data.models.ProductsFolder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoldersList(
    modifier: Modifier = Modifier,
    folders: List<ProductsFolder>,
    onAddImageClicked: (folderName: String) -> Unit = {},
    onDeleteImage: (folderName: String, imageUri: Uri) -> Unit = { _, _ ->},
    onDeleteFolderClicked: (folderName: String) -> Unit = {},
) {
    LazyColumn (
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),

    ){
        items(folders){ folder ->
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

