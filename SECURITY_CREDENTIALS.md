# Security Best Practices - OAuth Credentials

## ⚠️ IMPORTANT: Protecting Sensitive Credentials

### What Was Fixed

The OAuth client credentials were initially hardcoded in the source code, which is a **critical security vulnerability**. This has been fixed by:

1. **Removing credentials from code**: `GitHubOAuthConfig.kt` no longer contains hardcoded credentials
2. **Using properties file**: Credentials are now stored in `oauth.properties` (not committed to git)
3. **Git ignore configuration**: `oauth.properties` is in `.gitignore` to prevent accidental commits
4. **BuildConfig injection**: Android app loads credentials securely at build time

### Setup Instructions

1. **Copy the template**:
   ```bash
   cp oauth.properties.template oauth.properties
   ```

2. **Add your credentials** to `oauth.properties`:
   ```properties
   GITHUB_CLIENT_ID=your_client_id_here
   GITHUB_CLIENT_SECRET=your_client_secret_here
   ```

3. **Never commit** `oauth.properties`:
   - It's already in `.gitignore`
   - Always verify with: `git status --ignored`
   - Never use `git add -f oauth.properties`

### How It Works

#### Android
- Credentials are read from `oauth.properties` at build time
- Injected as `BuildConfig` constants
- Loaded in `MainActivity.onCreate()`
- Not included in source control

#### iOS/Desktop/Web
- Similar approach using build configuration
- Platform-specific secure storage
- Credentials loaded at runtime from secure config

### Production Deployment

**⚠️ Client secrets should NEVER be in mobile/desktop apps in production!**

For production, implement a backend OAuth proxy:

1. **Backend server** handles OAuth token exchange
2. **Mobile/desktop apps** receive tokens from your backend
3. **Client secret** stays on your secure server
4. **Apps only store** access tokens (encrypted)

Example flow:
```
User clicks "Login" 
  → App opens GitHub OAuth (with Client ID only)
  → User authorizes
  → GitHub redirects with code
  → App sends code to YOUR backend
  → Backend exchanges code for token (using client secret)
  → Backend returns token to app
  → App stores encrypted token
```

### What Not To Do

❌ **Never** commit credentials to git  
❌ **Never** hardcode credentials in source files  
❌ **Never** share credentials in issues/PRs  
❌ **Never** include client secrets in mobile apps (production)  
❌ **Never** push to public repositories without checking  

### What To Do

✅ **Always** use environment variables or properties files  
✅ **Always** add sensitive files to `.gitignore`  
✅ **Always** use BuildConfig or similar injection  
✅ **Always** implement backend OAuth for production  
✅ **Always** review commits before pushing  
✅ **Always** rotate credentials if exposed  

### If Credentials Are Exposed

If you accidentally commit credentials:

1. **Immediately revoke** the OAuth app on GitHub
2. **Create new** OAuth app with new credentials
3. **Remove from git history**:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch oauth.properties" \
     --prune-empty --tag-name-filter cat -- --all
   ```
4. **Force push** (if you own the repo)
5. **Notify team** to fetch clean history

### Additional Resources

- [GitHub OAuth Best Practices](https://docs.github.com/en/developers/apps/building-oauth-apps/best-practices-for-oauth-apps)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [Storing Secrets in Android](https://developer.android.com/training/articles/keystore)

---

**Remember**: Security is not optional. Always protect sensitive credentials! 🔒
