package com.wheremashrimps.reach.ui

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wheremashrimps.reach.data.AppInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** '#' sorts before A-Z. */
private val letterOrder = compareBy<Char> { if (it == '#') ' ' else it }

@Composable
fun LauncherScreen(vm: LauncherViewModel) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val favoriteKeys by vm.favoriteKeySet.collectAsStateWithLifecycle()
    val drawerOpen by vm.drawerOpen.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    val groups = remember(apps) {
        apps.groupBy { it.letter }.toSortedMap(letterOrder)
    }
    // Position of each letter's header in the drawer list (one header row, then its apps).
    val headerIndex = remember(groups) {
        var index = 0
        groups.mapValues { (_, list) -> index.also { index += 1 + list.size } }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val actions = remember(vm) {
        AppActions(
            onLaunch = vm::launch,
            onToggleFavorite = vm::toggleFavorite,
            onMove = vm::moveFavorite,
            onInfo = vm::openAppInfo,
            onUninstall = vm::uninstall,
        )
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = !drawerOpen, enter = fadeIn(), exit = fadeOut()) {
            HomePage(
                favorites = favorites,
                actions = actions,
                onSwipeUp = vm::openDrawer,
            )
        }

        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn() + slideInVertically { it / 8 },
            exit = fadeOut() + slideOutVertically { it / 8 },
        ) {
            Drawer(
                groups = groups,
                apps = apps,
                favoriteKeys = favoriteKeys,
                query = query,
                onQueryChange = vm::setQuery,
                listState = listState,
                actions = actions,
                onSwipeDownAtTop = vm::closeDrawer,
            )
        }

        if (query.isEmpty()) {
            AlphabetScroller(
                letters = groups.keys.toList(),
                onLetterSelected = { letter ->
                    headerIndex[letter]?.let { scope.launch { listState.scrollToItem(it) } }
                },
                onActiveChange = { active -> if (active) vm.openDrawer() },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .systemBarsPadding()
                    .padding(top = 96.dp, bottom = 48.dp, end = 4.dp),
            )
        }
    }
}

@Composable
private fun HomePage(
    favorites: List<AppInfo>,
    actions: AppActions,
    onSwipeUp: () -> Unit,
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { _, dy ->
                        dragTotal += dy
                        if (dragTotal < -120f) {
                            dragTotal = 0f
                            onSwipeUp()
                        }
                    },
                )
            }
            .systemBarsPadding()
            .padding(start = 28.dp, end = 56.dp),
    ) {
        ClockHeader(Modifier.padding(top = 48.dp))
        Spacer(Modifier.weight(1f))
        if (favorites.isEmpty()) {
            Text(
                text = "Long-press any app to add it here.\nSwipe up or slide the letters to see all apps.",
                color = Color.White.copy(alpha = 0.7f),
                style = TextStyle(fontSize = 15.sp, shadow = WallpaperTextShadow),
            )
        }
        favorites.forEachIndexed { i, app ->
            AppRow(
                app = app,
                isFavorite = true,
                actions = actions,
                large = true,
                canMoveUp = i > 0,
                canMoveDown = i < favorites.lastIndex,
            )
        }
        Spacer(Modifier.height(56.dp))
    }
}

@Composable
private fun ClockHeader(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"

    Column(modifier) {
        Text(
            text = now.format(DateTimeFormatter.ofPattern(timePattern)),
            color = Color.White,
            style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Light, shadow = WallpaperTextShadow),
        )
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            color = Color.White.copy(alpha = 0.85f),
            style = TextStyle(fontSize = 17.sp, shadow = WallpaperTextShadow),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Drawer(
    groups: Map<Char, List<AppInfo>>,
    apps: List<AppInfo>,
    favoriteKeys: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    listState: LazyListState,
    actions: AppActions,
    onSwipeDownAtTop: () -> Unit,
) {
    val results = remember(apps, query) {
        if (query.isBlank()) emptyList()
        else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
            .sortedByDescending { it.label.startsWith(query.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .statusBarsPadding()
            .imePadding(),
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search apps") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.12f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { results.firstOrNull()?.let(actions.onLaunch) }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )

        // Pulling down past the top of the list (scroll the list can't use) closes the drawer.
        val currentOnSwipeDown by rememberUpdatedState(onSwipeDownAtTop)
        val closeOnPull = remember {
            object : NestedScrollConnection {
                var pulled = 0f

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput && available.y > 0f) {
                        pulled += available.y
                        if (pulled > 150f) {
                            pulled = 0f
                            currentOnSwipeDown()
                        }
                    } else {
                        pulled = 0f
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    pulled = 0f
                    return Velocity.Zero
                }
            }
        }
        val listModifier = Modifier
            .fillMaxSize()
            .nestedScroll(closeOnPull)
            .navigationBarsPadding()
        val contentPadding = PaddingValues(start = 28.dp, end = 56.dp, bottom = 24.dp)

        if (query.isNotBlank()) {
            LazyColumn(contentPadding = contentPadding, modifier = listModifier) {
                items(results, key = { it.key }) { app ->
                    AppRow(app = app, isFavorite = app.key in favoriteKeys, actions = actions)
                }
            }
        } else {
            LazyColumn(state = listState, contentPadding = contentPadding, modifier = listModifier) {
                groups.forEach { (letter, groupApps) ->
                    stickyHeader(key = "header-$letter") {
                        Text(
                            text = letter.toString(),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                        )
                    }
                    items(groupApps, key = { it.key }) { app ->
                        AppRow(app = app, isFavorite = app.key in favoriteKeys, actions = actions)
                    }
                }
            }
        }
    }
}
