package com.zaed.barcodescanner.ui.main.components

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductImagesList(
    modifier: Modifier = Modifier,
    images: List<ProductImage>,
    onImageClicked: (Uri) -> Unit = {},
    onDeleteImage: (Uri) -> Unit = {},
) {
    AnimatedContent(targetState = images.isEmpty()) { state ->
        when {
            state -> {
                Text(
                    modifier = modifier.padding(top = 16.dp),
                    text = stringResource(R.string.no_images_added_yet),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            else -> {
                FlowRow(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    images.forEach { image ->
                        ProductImageItem(
                            image = image,
                            onImageClick = {onImageClicked(image.uri)},
                            onDeleteImage = { onDeleteImage(image.uri) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductImageItem(
    modifier: Modifier = Modifier,
    image: ProductImage,
    onDeleteImage: () -> Unit,
    onImageClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.size(height = 72.dp, width = 64.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant),
        tonalElevation = 0.1.dp,
        onClick = {onImageClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = image.uri,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low
                ),
                contentDescription = "Attachment BackGround",
                modifier = Modifier.fillMaxSize()
            )
            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = image.uploadProgress > 0 && image.uploadProgress < 1
            ) {
                Box(modifier=Modifier.fillMaxSize().background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if(it)0.5f else 0f)
                )){
                when {
                    it -> {
                        CircularProgressIndicator(
                            progress = { image.uploadProgress },
                            modifier = Modifier
                                .padding(top = 4.dp, end = 4.dp).align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    else -> {
                        if (!image.isUploaded) {
                            IconButton(
                                onClick = { onDeleteImage() },
                                modifier = Modifier
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(16.dp).align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "delete",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
                }

            }

        }
    }

}