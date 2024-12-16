package com.zaed.barcodescanner.ui.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.zaed.barcodescanner.data.models.ProductImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductImagesList(
    modifier: Modifier = Modifier,
    images: List<ProductImage>
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        images.forEach { image ->
            ProductImageItem(
                image = image
            )
        }
    }
}

@Composable
fun ProductImageItem(
    modifier: Modifier = Modifier,
    image: ProductImage,
) {
    Surface(
        modifier = modifier.size(100.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant),
        tonalElevation = 0.1.dp,
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = image.uri,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low
            ),
            contentDescription = "Attachment BackGround",
            modifier = Modifier.fillMaxSize().clickable {
//                onImageClicked(attachment.uri)
            }
        )
    }

}