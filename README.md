# Notification Vault 📬

A personal Android app that silently records every notification you receive — title, body, app name, and timestamp — so nothing is ever lost even after you dismiss it.

---

## Features

- **Background capture** — `NotificationListenerService` logs every notification in real time
- **Local storage** — Room (SQLite) database; no internet required, no data leaves your phone
- **Filter by app** — dropdown to show logs from a single app
- **⭐ Star** — mark important notifications so they survive bulk-delete
- **Search** — full-text search across title, body, and app name
- **Smart timestamps** — "2m ago", "3h ago", or date for older entries
- **Clear options** — delete unstarred only, or wipe everything

---

## Setup in Android Studio

1. Open Android Studio → **File → Open** → select the `NotificationVault` folder
2. Let Gradle sync finish (first time may take a minute)
3. Connect your phone or start an emulator
4. Run the app (**Shift+F10** or the ▶ button)
5. Tap the red banner → **Notification Vault Logger** → toggle ON → back
6. Done — notifications will now be captured automatically

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/shs/notificationvault/
│   ├── MainActivity.kt          ← UI + ViewModel wiring
│   ├── MainViewModel.kt         ← Filter/search state
│   ├── NotificationLogService.kt ← Background listener ★
│   ├── data/
│   │   ├── NotificationEntity.kt  ← Room table model
│   │   ├── NotificationDao.kt     ← SQL queries
│   │   ├── NotificationDatabase.kt ← Room singleton
│   │   └── NotificationRepository.kt ← Data layer
│   └── ui/
│       └── NotificationAdapter.kt ← RecyclerView
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   └── item_notification.xml
    ├── menu/menu_main.xml
    └── values/
        ├── strings.xml
        └── themes.xml
```

---

## Play Store Readiness Checklist (when you're ready)

- [ ] Replace default launcher icons in `res/mipmap-*/`
- [ ] Add a Privacy Policy (required for apps using Notification Access)
- [ ] Update `versionCode` and `versionName` in `app/build.gradle`
- [ ] Enable `minifyEnabled true` in release build type
- [ ] Generate a signed APK / AAB via Build → Generate Signed Bundle

---

## Notes

- **What is skipped:** Your own app's notifications, `android` system, `com.android.systemui`, and `isOngoing` (persistent) notifications
- **minSdk 26** = Android 8.0+ (covers ~95% of active Android devices)
- The database file is `notification_vault.db` in the app's private storage — uninstalling the app wipes it
