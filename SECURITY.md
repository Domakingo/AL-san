# Security Policy for AL-san

## Security Features Implemented

This fork of AL-chan includes several security improvements:

### 1. Encrypted Token Storage
- All sensitive data (bearer tokens, API keys) are now stored using **EncryptedSharedPreferences**
- Uses AES-256-GCM encryption with keys stored in Android Keystore
- Protects against data extraction via ADB backup or root access

### 2. Backup Disabled
- `android:allowBackup="false"` prevents attackers from extracting app data via ADB backup
- This is critical for protecting authentication tokens

### 3. Network Security Configuration
- All network traffic is restricted to HTTPS only (cleartext blocked)
- Domain-specific configurations for AniList, Spotify, YouTube, and other APIs
- Ready for certificate pinning implementation

### 4. HTTP Logging Security
- HTTP request/response logging is **disabled in production** builds
- Only enabled in debug builds for development purposes
- Sensitive headers (Authorization, Cookie) are redacted even in debug builds

### 5. Deep Link Validation
- OAuth callback deep links are properly validated
- Protection against malformed URLs that could cause crashes

### 6. Updated Dependencies
All dependencies have been updated to address known CVEs:
- OkHttp 4.12.0 (CVE-2023-3635 fix)
- Retrofit 2.11.0
- Kotlin 2.0.21
- Coil 2.7.0
- androidx.security:security-crypto 1.1.0-alpha06

## Reporting a Vulnerability

If you discover a security vulnerability in AL-san, please:

1. **Do NOT** open a public issue
2. Send details to the maintainer privately
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

## Security Best Practices for Users

1. **Keep the app updated** to receive the latest security patches
2. **Don't install APKs from untrusted sources** - only use official releases
3. **Logout when not using** the app on shared devices
4. **Revoke API access** on AniList settings if you suspect account compromise

## For Developers

When contributing to this project:

1. **Never log sensitive data** - use `redactHeader()` for authorization headers
2. **Use EncryptedSharedPreferences** for any new sensitive data storage
3. **Validate all external input** - especially from deep links and API responses
4. **Keep dependencies updated** - regularly check for CVEs
5. **Follow Android security best practices** - [Android Security Guidelines](https://developer.android.com/topic/security/best-practices)
