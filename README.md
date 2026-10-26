# Reach

A minimal, one-handed Android home screen inspired by list-style launchers.
Built with Kotlin and Jetpack Compose.

## What's in v0.1

- **Home page:** clock and date at the top, your favorite apps as a large list near your thumb.
- **Alphabet scroller:** touch or slide along the right edge and the full app list jumps to that letter, with a wave animation and haptic ticks.
- **App drawer:** swipe up (or touch the letters) for every app, grouped by letter. Pull down at the top to close.
- **Search:** filter apps as you type; the keyboard's Go key opens the top result.
- **Long-press menu:** add or remove a favorite, reorder favorites, App info, Uninstall.
- **Work profile and Secure Folder apps** are included, with their badges.
- Uses your wallpaper and Material You colors. Home closes the drawer, and Back never exits the launcher.

## Build and install

Requirements: Android Studio (Ladybug or newer) or the Android SDK with JDK 17+.

```sh
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open the folder in Android Studio and press Run with your phone connected (USB debugging on).

Then on the phone: **Settings → Apps → Choose default apps → Home app → Reach**.

## Roadmap

- Notification previews under each app (needs notification access)
- Swipe an app for its shortcuts
- Media player card and weather in the header
- Swipe down to open the notification shade
- Settings: icon packs, fonts, hide apps, list position
