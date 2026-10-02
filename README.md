# Product Hunt Viewer

A native Android app for discovering and filtering product launches published on Product Hunt.

## Features

- Browse launches in a paginated feed.
- Sort by ranking, newest, vote count, or featured date.
- Filter by Android, iOS, Desktop, or Web platform.
- Filter by topic, featured status, date range, Product Hunt URL, or Twitter/X URL.
- Quickly show launches posted today.
- View launch thumbnails, taglines, topics, makers, votes, and comments.
- Open a launch on Product Hunt.
- Store and replace a Product Hunt developer token locally on the device.

## Requirements

- Android 10 (API 29) or newer.
- A valid Product Hunt developer token.
- Android Studio and JDK 17 to build from source.

## Getting started

1. Build and install the app on an Android device or emulator.
2. Enter your Product Hunt developer token when prompted.
3. Browse launches or use the filter button to narrow the feed.
4. Tap a launch to open it on Product Hunt.

The token is validated before being saved and is stored locally using Android secure-storage APIs. No developer token is included in this repository.

## Build from source

```bash
./gradlew assembleDebug
```

Release APKs for common Android architectures are in [`apk/release/`](apk/release/). For most devices, use [`app-universal-release.apk`](apk/release/app-universal-release.apk). To sign your own release build, provide your own keystore and local `release.properties`; signing credentials are intentionally not included.

## Product Hunt API

This app uses the [Product Hunt GraphQL API v2](https://api.producthunt.com/v2/docs). Users must provide their own developer token and follow Product Hunt’s API terms and policies.

## License

This project is licensed under the **GNU General Public License v3.0 only (GPL-3.0-only)**. See [`LICENSE`](LICENSE) for the complete license text.
