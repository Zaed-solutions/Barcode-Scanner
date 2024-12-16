package com.zaed.barcodescanner.ui.main.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zaed.barcodescanner.R
import com.zaed.barcodescanner.data.models.ProductsFolder

@Composable
fun FolderItem(
    modifier: Modifier = Modifier,
    folder: ProductsFolder,
    onAddImageClicked: () -> Unit,
    onDeleteImage: (Uri) -> Unit = {},
    onDeleteFolderClicked: () -> Unit,
) {
    var isExpanded by remember{
        mutableStateOf(false)
    }
    val anim = remember {
        Animatable(0f)
    }
    LaunchedEffect(isExpanded) {
        anim.animateTo(
            targetValue = if (isExpanded) 180f else 0f
        )
    }
    Surface (
        modifier = modifier,
        onClick = {
            isExpanded = !isExpanded
        },
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
    ){
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            //Header with name and actions
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ){
                Column (
                    modifier = Modifier.weight(1f),
                ){
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    AnimatedVisibility(visible = !isExpanded) {
                        Text(
                            text = stringResource(R.string.images_temp, folder.images.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDeleteFolderClicked
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(
                    onClick = onAddImageClicked
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                ProductImagesList(
                    images = folder.images,
                    onDeleteImage = onDeleteImage
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(anim.value)
            )
        }
    }
}