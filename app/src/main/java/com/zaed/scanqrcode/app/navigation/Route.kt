package com.zaed.scanqrcode.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object DefaultRoute : Route
}