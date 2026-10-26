package com.wheremashrimps.reach.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wheremashrimps.reach.data.AppInfo
import com.wheremashrimps.reach.data.AppRepository
import com.wheremashrimps.reach.data.BackgroundStore
import com.wheremashrimps.reach.data.FavoritesStore
import com.wheremashrimps.reach.data.PhotoColors
import com.wheremashrimps.reach.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AppRepository(app)
    private val favoritesStore = FavoritesStore(app)
    private val settings = SettingsStore(app)
    private val backgroundStore = BackgroundStore(app)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val favoriteKeys = MutableStateFlow(favoritesStore.load())

    /** Favorites in the user's order, skipping any that were uninstalled. */
    val favorites: StateFlow<List<AppInfo>> = combine(_apps, favoriteKeys) { apps, keys ->
        val byKey = apps.associateBy { it.key }
        keys.mapNotNull { byKey[it] }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteKeySet: StateFlow<Set<String>> = favoriteKeys.map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val _drawerOpen = MutableStateFlow(false)
    val drawerOpen: StateFlow<Boolean> = _drawerOpen.asStateFlow()

    private val _theme = MutableStateFlow(settings.theme)
    val theme: StateFlow<LauncherTheme> = _theme.asStateFlow()

    private val _photo = MutableStateFlow<ImageBitmap?>(null)
    val photo: StateFlow<ImageBitmap?> = _photo.asStateFlow()

    private val _photoColors = MutableStateFlow(settings.photoColors)
    val photoColors: StateFlow<PhotoColors?> = _photoColors.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private var loadJob: Job? = null
    private val unregister = repository.observeChanges { reload() }

    init {
        reload()
        viewModelScope.launch {
            _photo.value = withContext(Dispatchers.IO) { backgroundStore.load() }?.asImageBitmap()
        }
    }

    private fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val loaded = withContext(Dispatchers.Default) { repository.loadApps() }
            _apps.value = loaded
            seedFavoritesIfNeeded(loaded)
        }
    }

    private fun seedFavoritesIfNeeded(apps: List<AppInfo>) {
        if (favoritesStore.isInitialized || apps.isEmpty()) return
        val defaults = FavoritesStore.DEFAULT_PACKAGES
            .mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg }?.key }
            .take(FavoritesStore.MAX_DEFAULTS)
        setFavorites(defaults)
    }

    private fun setFavorites(keys: List<String>) {
        favoriteKeys.value = keys
        favoritesStore.save(keys)
    }

    fun toggleFavorite(app: AppInfo) {
        val keys = favoriteKeys.value
        setFavorites(if (app.key in keys) keys - app.key else keys + app.key)
    }

    fun moveFavorite(app: AppInfo, delta: Int) {
        val keys = favoriteKeys.value.toMutableList()
        val from = keys.indexOf(app.key)
        val to = from + delta
        if (from < 0 || to !in keys.indices) return
        keys.add(to, keys.removeAt(from))
        setFavorites(keys)
    }

    fun launch(app: AppInfo) {
        repository.launch(app)
        closeDrawer()
    }

    fun openAppInfo(app: AppInfo) = repository.openAppInfo(app)

    fun uninstall(app: AppInfo) = repository.uninstall(app)

    fun openDrawer() {
        _drawerOpen.value = true
    }

    fun closeDrawer() {
        _drawerOpen.value = false
        _query.value = ""
    }

    fun setTheme(theme: LauncherTheme) {
        settings.theme = theme
        _theme.value = theme
    }

    /** Uses the picked photo as the background, themes the launcher from its colors, and switches to it. */
    fun setPhoto(uri: Uri) {
        Log.i(TAG, "Loading background photo from $uri")
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { backgroundStore.save(uri)?.let { it to BackgroundStore.extractColors(it) } }
            }
            val loaded = result.getOrElse { error ->
                Log.e(TAG, "Couldn't load background photo", error)
                toast("Couldn't load that photo (${error.javaClass.simpleName}: ${error.message})")
                return@launch
            }
            if (loaded == null) {
                Log.e(TAG, "Background photo could not be decoded: $uri")
                toast("Couldn't read that photo. Try downloading it to your phone first.")
                return@launch
            }
            val (bitmap, colors) = loaded
            settings.photoColors = colors
            _photoColors.value = colors
            _photo.value = bitmap.asImageBitmap()
            setTheme(LauncherTheme.PHOTO)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show()
    }

    fun setQuery(text: String) = _query.update { text }

    private companion object {
        const val TAG = "Reach"
    }

    override fun onCleared() {
        unregister()
    }
}
