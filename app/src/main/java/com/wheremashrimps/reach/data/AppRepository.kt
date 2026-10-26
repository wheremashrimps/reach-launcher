package com.wheremashrimps.reach.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import java.text.Collator

data class AppInfo(
    /** Stable id used for favorites: component plus the profile's serial number. */
    val key: String,
    val label: String,
    val component: ComponentName,
    val user: UserHandle,
    val icon: ImageBitmap,
) {
    val packageName: String get() = component.packageName

    /** Index letter for the alphabet scroller; anything that isn't A-Z goes under "#". */
    val letter: Char
        get() {
            val c = label.firstOrNull()?.uppercaseChar() ?: '#'
            return if (c in 'A'..'Z') c else '#'
        }
}

class AppRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)
    private val iconSizePx = (48 * context.resources.displayMetrics.density).toInt()
    private val collator = Collator.getInstance()

    /** Every launchable activity across all profiles (personal, work, Secure Folder), sorted by label. */
    fun loadApps(): List<AppInfo> {
        val apps = userManager.userProfiles.flatMap { user ->
            val serial = userManager.getSerialNumberForUser(user)
            // A locked profile (e.g. Secure Folder) can refuse to be listed; skip it rather than fail.
            runCatching { launcherApps.getActivityList(null, user) }.getOrDefault(emptyList())
                .filter { it.componentName.packageName != context.packageName }
                .map { info ->
                    AppInfo(
                        key = "${info.componentName.flattenToString()}#$serial",
                        label = info.label.toString().trim(),
                        component = info.componentName,
                        user = user,
                        icon = info.getBadgedIcon(0)
                            .toBitmap(iconSizePx, iconSizePx)
                            .asImageBitmap(),
                    )
                }
        }
        return apps.sortedWith { a, b -> collator.compare(a.label, b.label) }
    }

    /** Calls [onChange] whenever an app is installed, removed or updated. Returns an unregister function. */
    fun observeChanges(onChange: () -> Unit): () -> Unit {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) = onChange()
            override fun onPackageAdded(packageName: String, user: UserHandle) = onChange()
            override fun onPackageChanged(packageName: String, user: UserHandle) = onChange()
            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = onChange()
            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = onChange()
        }
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        return { launcherApps.unregisterCallback(callback) }
    }

    fun launch(app: AppInfo) {
        launcherApps.startMainActivity(app.component, app.user, null, null)
    }

    fun openAppInfo(app: AppInfo) {
        launcherApps.startAppDetailsActivity(app.component, app.user, null, null)
    }

    fun uninstall(app: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
            .putExtra(Intent.EXTRA_USER, app.user)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
