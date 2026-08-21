# crash-kit-android

Production **CrashKit** for Android:

| Module | Backend | Artifact |
|---|---|---|
| `crash-kit-global` | Firebase Crashlytics | AAR (no Umeng) |
| `crash-kit-china` | Umeng U-APM | AAR (no Firebase) |
| `demo-global` | Uses global kit | APK |
| `demo-china` | Uses china kit | APK |

**Same package API:** `com.bettythegoodboi.crashkit.CrashKit`

Vendor picks **one** AAR per product region build. Do not mix both SDKs in one APK.

## CI artifacts

[Actions](../../actions) publishes:

- `crash-kit-global-release.aar`
- `crash-kit-china-release.aar`
- `demo-global-debug.apk`
- `demo-china-debug.apk`

## Docs

- [Vendor integration (full)](docs/VENDOR_INTEGRATION.md)

## Note

- Global demo needs valid `demo-global/google-services.json` (Firebase).
- China demo AppKey is set in `demo-china` BuildConfig (Umeng).
- Production: call `init` only after privacy consent.
