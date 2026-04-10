package com.scoreeboard.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route objects for Navigation Compose 2.8+.
 * No arguments are passed between screens because all state lives
 * in the shared GameViewModel scoped to the Activity.
 */
@Serializable object SetupRoute
@Serializable object GameRoute
@Serializable object SummaryRoute
@Serializable object HistoryRoute
