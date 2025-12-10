# GitHub Followy

A Kotlin Multiplatform app that helps you manage your GitHub followers across Android, iOS, Desktop, and **Web** platforms.

## Features

- 🔐 **GitHub OAuth Authentication** - Login with your GitHub Personal Access Token
- 👥 **Follower Analysis** - See who follows you but you don't follow back
- 🔄 **Following Analysis** - See who you follow but they don't follow back
- ➕ **Follow Users** - Follow users directly from the app
- ➖ **Unfollow Users** - Unfollow users with a single tap
- 📱 **Cross-Platform** - Works on Android, iOS, Desktop, and Web

## Setup

### Prerequisites

- JDK 17 or higher
- Android Studio or IntelliJ IDEA
- Xcode (for iOS development)
- GitHub Personal Access Token with the following scopes:
  - `user` - Read user profile data
  - `user:follow` - Follow and unfollow users

### Creating a GitHub Personal Access Token

1. Go to [GitHub Settings > Tokens](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Select the following scopes:
   - `user`
   - `user:follow`
4. Click "Generate token"
5. Copy the token (you'll need it to login to the app)

### Building the Project

#### Android

```bash
./gradlew :androidApp:assembleDebug
```

#### Desktop

```bash
./gradlew :desktopApp:run
```

#### iOS

Open the `iosApp/templateIOS.xcodeproj` in Xcode and run the project.

#### Web

```bash
./gradlew :shared:jsRun
```

Or build for production:

```bash
./gradlew :shared:jsBrowserDistribution
```

The output will be in `shared/build/dist/js/productionExecutable/`

    ├── androidMain/     # Android-specific implementations
    ├── iosMain/         # iOS-specific implementations
    ├── desktopMain/     # Desktop-specific implementations
    ├── jsMain/          # Web (JavaScript) implementations
    └── wasmJsMain/      # Web (WebAssembly) implementations
``` androidApp/          # Android-specific code
├── desktopApp/          # Desktop-specific code  
├── iosApp/              # iOS-specific code
└── shared/              # Shared KMP code
    ├── commonMain/      # Common code for all platforms
    │   ├── data/
    │   │   ├── api/     # GitHub API client
    │   │   ├── models/  # Data models
    │   │   └── repository/ # Repository layer
    │   ├── di/          # Dependency injection
    │   └── ui/          # Compose UI screens
    ├── androidMain/     # Android-specific implementations
    ├── iosMain/         # iOS-specific implementations
    └── desktopMain/     # Desktop-specific implementations
```

## Technologies Used

- **Kotlin Multiplatform** - Share code across platforms
- **Compose Multiplatform** - UI framework
- **Ktor** - Networking
- **Koin** - Dependency injection
- **Kotlinx Serialization** - JSON parsing
- **Coil** - Image loading
- **Coroutines & Flow** - Asynchronous programming

## Usage

1. Launch the app
2. Enter your GitHub Personal Access Token
3. Click "Login"
4. View your follower analytics in two tabs:
   - **Follow You Back** - Users who follow you but you don't follow back
   - **Not Following Back** - Users you follow but they don't follow back
5. Use the "Follow" or "Unfollow" buttons to manage your connections
6. Pull to refresh to update the data

## API Rate Limits

GitHub API has rate limits:
- Authenticated requests: 5,000 requests per hour
- The app fetches all followers and following in batches of 100

## Future Enhancements

- OAuth web flow authentication
- Local caching with SQLDelight
- Batch follow/unfollow operations
- User search functionality
- Filter and sort options
- Export follower data
- Dark mode support
- Web platform support ✅ **DONE!**

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
