package com.zaed.scanqrcode.ui.util

import androidx.compose.ui.graphics.Color


fun Color.Companion.fromHex(colorString: String) = Color(android.graphics.Color.parseColor(colorString))