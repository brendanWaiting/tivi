// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.analytics

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.settings.TiviPreferences
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AnalyticsInitializer(
  private val preferences: TiviPreferences,
  private val scope: ApplicationCoroutineScope,
  private val analytics: Analytics,
) : AppInitializer {
  override fun initialize() {
    scope.launch {
      preferences.observeReportAnalytics().collect { enabled ->
        analytics.setEnabled(enabled)
      }
    }
  }
}
