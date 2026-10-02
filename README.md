# Product Hunt Viewer

Product Hunt Viewer is a native Android app for discovering and exploring products launched on Product Hunt.

The app connects to the Product Hunt GraphQL API and presents product launches in a clean, mobile-friendly feed. It is designed for people who want to browse new products, find launches by topic or platform, and quickly open the original Product Hunt page.

## Features

- Browse Product Hunt launches in a paginated feed.
- Sort launches by ranking, newest, votes, or featured date.
- Filter by platform: Android, iOS, Desktop, or Web.
- Filter by Product Hunt topic.
- Show only today's launches.
- Filter featured or non-featured launches.
- Search by date range, Product Hunt URL, or Twitter/X URL.
- View launch thumbnails, taglines, topics, makers, vote counts, and comment counts.
- Open the original Product Hunt page from any launch.
- Store the Product Hunt developer token securely on the device.
- Sign out and switch tokens at any time.

## Screenshots

Screenshots can be added here as the project evolves.

## Requirements

- Android 10 (API 29) or newer.
- A valid Product Hunt developer token.
- Android Studio with JDK 17 for building the project.

## Getting started

1. Build and install the app on an Android device or emulator.
2. On first launch, enter a Product Hunt developer token.
3. Browse the latest launches or open the filter panel to refine the feed.
4. Tap a launch to open its Product Hunt page.

The token is validated before it is saved and is stored locally using Android's secure storage facilities. No token is included in this repository.

## Building from source

```bash
./gradlew assembleDebug
```

The release APKs included in [`apk/release/`](apk/release/) are built for common Android architectures. For most devices, use [`app-universal-release.apk`](apk/release/app-universal-release.apk).

To create your own signed release build, configure a local `release.properties` file and provide your own keystore. Signing files and passwords are intentionally excluded from version control.

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Security Crypto
- OkHttp
- Coil
- Product Hunt GraphQL API v2

## API access

This application uses the [Product Hunt API](https://api.producthunt.com/v2/docs). You must provide your own developer token and comply with Product Hunt's terms and API policies.

## License

No license has been selected yet. Until a license is added, all rights are reserved by the copyright holder.
