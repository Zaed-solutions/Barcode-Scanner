package com.zaed.barcodescanner.ui.main.components

import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductsFolder

@Composable
fun FolderItem(
    modifier: Modifier = Modifier,
    folder: ProductsFolder,
    onAddImageClicked: () -> Unit = {},
    onDeleteImage: (Uri) -> Unit = {},
    onDeleteFolderClicked: () -> Unit = {},
    onUploadClicked: () -> Unit = {},
    onImageClicked: (Uri) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(isExpanded) {
        rotationAnim.animateTo(if (isExpanded) 180f else 0f)
    }

    Surface(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .animateContentSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(
                        R.string.images_uploaded,
                        folder.images.filter { it.isUploaded }.size,
                        folder.images.size
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = onAddImageClicked
                ) {

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Add Image",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.add),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                }
                Log.d("UPLOAD_SUCCESS", "UploadIconButton: ${folder.images}")
                UploadIconButton(
                    modifier = Modifier.weight(1f),
                    isUploaded = folder.images.all { it.isUploaded } && folder.images.isNotEmpty(),
                    onUploadClicked
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDeleteFolderClicked
                ) {

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Folder",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                }

            }
            // Expandable Content
            if (isExpanded) {
                ProductImagesList(
                    images = folder.images,
                    onDeleteImage = onDeleteImage,
                    onImageClicked = onImageClicked
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand/Collapse",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .rotate(rotationAnim.value)
            )
        }
    }
}

@Composable
fun UploadIconButton(
    modifier: Modifier = Modifier,
    isUploaded: Boolean = false,
    onUploadClicked: () -> Unit = {}
) {
    TextButton(
        modifier = modifier,
        onClick = onUploadClicked,
    ) {

        Icon(
            imageVector = if (isUploaded) Icons.Default.Check else Icons.Default.DriveFolderUpload,
            contentDescription = null,
            tint = if (isUploaded) Color.Green else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.upload),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

    }
}

@Preview(locale = "ar")
@Composable
fun FolderItemPreview() {
    MaterialTheme {
        FolderItem(
            folder = ProductsFolder(
                name = "My Folder",
                images = listOf(
                    com.zaed.barcodescanner.data.models.ProductImage(Uri.EMPTY),
                    com.zaed.barcodescanner.data.models.ProductImage(Uri.EMPTY)
                )
            )
        )
    }
}