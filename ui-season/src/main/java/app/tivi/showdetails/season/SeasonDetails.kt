/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.showdetails.season

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.rememberFlowWithLifecycle
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.common.compose.ui.SwipeDismissSnackbar
import app.tivi.data.entities.Episode
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun SeasonDetails(
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
) {
    SeasonDetails(
        viewModel = hiltViewModel(),
        navigateUp = navigateUp,
        openEpisodeDetails = openEpisodeDetails,
    )
}

@Composable
internal fun SeasonDetails(
    viewModel: SeasonDetailsViewModel,
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = SeasonDetailsViewState.Empty)

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewState.refreshError) {
        viewState.refreshError?.let { error ->
            scaffoldState.snackbarHostState.showSnackbar(error.message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val seasonTitle = viewState.season.title
                    val seasonNumber = viewState.season.number
                    Text(
                        text = when {
                            seasonTitle != null -> seasonTitle
                            seasonNumber != null -> {
                                stringResource(R.string.season_title_fallback, seasonNumber)
                            }
                            else -> ""
                        }
                    )
                },
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false
                ),
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_up)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface.copy(
                    alpha = AppBarAlphas.translucentBarAlpha()
                ),
            )
        },
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    SwipeDismissSnackbar(
                        data = snackbarData,
                        onDismiss = { viewModel.clearError() }
                    )
                },
                modifier = Modifier
                    .padding(horizontal = Layout.bodyMargin)
                    .fillMaxWidth()
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
        ) {
            items(viewState.episodes, key = { it.episode.id }) { item ->
                EpisodeWithWatchesRow(
                    episode = item.episode,
                    isWatched = item.hasWatches,
                    hasPending = item.hasPending,
                    onlyPendingDeletes = item.onlyPendingDeletes,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clickable { openEpisodeDetails(item.episode.id) },
                )
            }
        }
    }
}

@Composable
private fun EpisodeWithWatchesRow(
    episode: Episode,
    isWatched: Boolean,
    hasPending: Boolean,
    onlyPendingDeletes: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = textCreator.episodeNumberText(episode).toString(),
                style = MaterialTheme.typography.caption
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = episode.title
                    ?: stringResource(R.string.episode_title_fallback, episode.number!!),
                style = MaterialTheme.typography.body2
            )
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            var needSpacer = false
            if (hasPending) {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud_upload),
                    contentDescription = stringResource(R.string.cd_episode_syncing),
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                needSpacer = true
            }
            if (isWatched) {
                if (needSpacer) Spacer(Modifier.width(4.dp))

                Icon(
                    painter = painterResource(
                        when {
                            onlyPendingDeletes -> R.drawable.ic_visibility_off
                            else -> R.drawable.ic_visibility
                        }
                    ),
                    contentDescription = when {
                        onlyPendingDeletes -> stringResource(R.string.cd_episode_deleted)
                        else -> stringResource(R.string.cd_episode_watched)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}
