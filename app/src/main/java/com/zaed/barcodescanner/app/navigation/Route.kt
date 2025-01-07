package com.zaed.barcodescanner.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object DefaultRoute : Route

    @Serializable
    data object MainRoute : Route

    @Serializable
    data object ManageAccountRoute : Route

    @Serializable
    data object CameraRoute : Route

    @Serializable
    data object SearchRoute : Route



}