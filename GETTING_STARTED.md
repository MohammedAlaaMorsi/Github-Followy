# GitHub Followy - Quick Start Guide

## What This App Does

GitHub Followy is a cross-platform application that helps you manage your GitHub followers. It shows you:

1. **People who follow you but you don't follow back** - So you can decide if you want to follow them
2. **People you follow but they don't follow back** - So you can decide if you want to unfollow them

You can follow/unfollow users directly from the app with a single click.

## How to Get Started

### Step 1: Create a GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Give it a name like "GitHub Followy App"
4. Select these permissions:
   - ✅ `user` (to read your profile)
   - ✅ `user:follow` (to follow/unfollow users)
5. Click "Generate token"
6. **IMPORTANT**: Copy the token immediately (you won't see it again!)

### Step 2: Run the App

#### On Android:
```bash
./gradlew :androidApp:installDebug
```
Then open the app on your device.

#### On Desktop:
```bash
./gradlew :desktopApp:run
```

#### On iOS:
1. Open `iosApp/templateIOS.xcodeproj` in Xcode
2. Click the Run button

#### On Web:
```bash
./gradlew :shared:jsRun
```
Then open http://localhost:8080 in your browser.

### Step 3: Login

1. Paste your GitHub token into the login screen
2. Click "Login"
3. Wait for the app to load your follower data

### Step 4: Manage Your Followers

- Switch between the two tabs to see different follower lists
- Click "Follow" to follow someone
- Click "Unfollow" to unfollow someone
- Click the refresh icon to reload data

## Project Architecture

The app follows clean architecture principles:

```
Data Layer (API + Repository)
    ↓
ViewModel Layer (Business Logic)
    ↓
UI Layer (Compose Multiplatform)
```

### Key Components:

- **GitHubApiClient**: Handles all GitHub API calls
- **GitHubRepository**: Business logic for follower analysis
- **AuthViewModel**: Manages authentication state
- **DashboardViewModel**: Manages dashboard state and follow/unfollow actions
- **LoginScreen**: The authentication UI
- **DashboardScreen**: The main dashboard UI

## Troubleshooting

### "Authentication failed"
- Make sure your token has the correct permissions (`user` and `user:follow`)
- Check if the token has expired

### "Failed to load dashboard"
- Check your internet connection
- You might have hit GitHub's rate limit (5,000 requests/hour)

### Build errors
- Make sure you have JDK 17 or higher
- Run `./gradlew clean` and try again

## Next Steps

Now that you have the basic app working, you can:

1. Add local caching with SQLDelight (already in dependencies)
2. Implement OAuth web flow instead of token input
3. Add batch operations (follow/unfollow multiple users)
4. Add user search functionality
5. Add filters and sorting options
6. Implement dark mode

Happy coding! 🚀
