# OAuth Setup Guide

## GitHub App Setup (Recommended)

To support multiple platforms (Android, iOS, Desktop) with a single configuration, it is highly recommended to use a **GitHub App** instead of an OAuth App. GitHub Apps allow multiple callback URLs.

### 1. Create a GitHub App

1. Go to [GitHub Settings > Developer settings > GitHub Apps](https://github.com/settings/apps)
2. Click **"New GitHub App"**
3. Fill in the application details:
   - **GitHub App name**: GitHub Followy (or your preferred name)
   - **Homepage URL**: `http://localhost:3000`
   - **Callback URL**: 
     - First URL: `githubfollowy://oauth/callback` (for Mobile)
     - Click "Add callback URL" and add: `http://127.0.0.1:8080/oauth/callback` (for Desktop)
   - **Setup URL & Webhook**: Uncheck "Active" for Webhooks if you don't need them.
4. **Permissions**:
   - **Account permissions**:
     - `Followers`: Read & Write (to follow/unfollow)
     - `Profile`: Read-only (to get user info)
5. Click **"Create GitHub App"**
6. Generate a **Client Secret** and note it down along with the **Client ID**.

---

## Alternative: GitHub OAuth Application Setup

> [!IMPORTANT]
> OAuth Apps only support ONE callback URL. If you use this, you'll need to choose one platform or use the Manual Entry fallback on Desktop.

1. Go to [GitHub Settings > Developer settings > OAuth Apps](https://github.com/settings/developers)
2. Click **"New OAuth App"**
3. **Authorization callback URL**: `githubfollowy://oauth/callback`

---

### Configure the App

Place your credentials in a `oauth.properties` file in the project root:

```properties
GITHUB_CLIENT_ID=your_client_id
GITHUB_CLIENT_SECRET=your_client_secret
```

### Platform-Specific Setup

#### Android/iOS
The apps are pre-configured to handle the `githubfollowy://` custom scheme.

#### Desktop
The desktop app will attempt to automatically capture the login via a local server on `http://127.0.0.1:8080/oauth/callback`. 

If you are using an **OAuth App** (not a GitHub App) and the desktop redirect fails, click **"Trouble logging in? Try Manual Entry"** on the login screen and paste the code from your browser's address bar.

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
