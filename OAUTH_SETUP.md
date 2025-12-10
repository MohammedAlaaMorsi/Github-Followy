# OAuth Setup Guide

## GitHub OAuth Application Setup

To enable "Login with GitHub" functionality, you need to register an OAuth application with GitHub:

### 1. Create a GitHub OAuth App

1. Go to [GitHub Settings > Developer settings > OAuth Apps](https://github.com/settings/developers)
2. Click **"New OAuth App"**
3. Fill in the application details:
   - **Application name**: GitHub Followy (or your preferred name)
   - **Homepage URL**: `http://localhost:3000` (for development)
   - **Authorization callback URL**: 
     - Android: `githubfollowy://oauth/callback`
     - iOS: `githubfollowy://oauth/callback`
     - Desktop: `http://localhost:8080/oauth/callback`
     - Web: `https://yourdomain.com/oauth/callback` (or `http://localhost:3000/oauth/callback` for dev)

4. Click **"Register application"**
5. Note down your **Client ID** and **Client Secret**

### 2. Configure the App

When you first click "Login with GitHub", the app will prompt you to enter:
- **Client ID**: From step 5 above
- **Client Secret**: From step 5 above

These credentials are stored locally and used for the OAuth flow.

### 3. Platform-Specific Setup

#### Android
Add this to your `AndroidManifest.xml`:
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="githubfollowy"
        android:host="oauth" />
</intent-filter>
```

#### iOS
Add this to your `Info.plist`:
```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>githubfollowy</string>
        </array>
    </dict>
</array>
```

#### Desktop
The app will open your system browser. After authorization, you may need to manually copy the callback URL.

#### Web
Ensure your redirect URI matches your hosting domain.

## Alternative: Personal Access Token

If you prefer not to set up OAuth, you can still use a Personal Access Token:

1. Go to [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
2. Click **"Generate new token (classic)"**
3. Select scopes:
   - `user` - Read user profile data
   - `user:follow` - Follow/unfollow users
4. Generate and copy the token
5. In the app, click "Use Personal Access Token instead"
6. Paste your token

## Security Notes

- OAuth tokens are obtained securely through GitHub's authorization flow
- Access tokens are encrypted using platform-specific secure storage:
  - **Android**: AES-256-GCM with hardware-backed keystore
  - **iOS**: Keychain Services
  - **Desktop**: AES-256-GCM encryption
  - **Web**: Browser localStorage with obfuscation
- Client secrets should never be hardcoded in production apps
- For production, implement a backend service to handle OAuth token exchange

## Troubleshooting

### OAuth not working?
- Ensure your callback URL in GitHub matches exactly
- Check that your Client ID and Secret are correct
- Verify your app has the required permissions

### Still having issues?
- Use the "Personal Access Token" option as a fallback
- Check browser console/app logs for error messages
