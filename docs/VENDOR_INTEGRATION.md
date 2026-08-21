# CrashKit — Vendor Integration Guide

**Class:** `com.bettythegoodboi.crashkit.CrashKit`

Two builds only. **Do not put both AARs in one APK.**

| Build | AAR file | Backend |
|---|---|---|
| **Global** | `crash-kit-global-release.aar` | Firebase Crashlytics |
| **China** | `crash-kit-china-release.aar` | Umeng U-APM |

If the product ships both markets → two APKs (or two product flavors), each following the matching section below.

Crash data sits on the **owner’s** Firebase / Umeng project. Vendor must have console login (Phase 0).

---

## Phase 0 — Vendor sends owner

| Item | When |
|---|---|
| Android `applicationId` (package name) | Always |
| **Google account email** (Gmail / Google Workspace) | **Global** — owner adds it to Firebase so vendor can open Crashlytics |
| **Umeng account** (email / login) | **China** — owner invites it so vendor can open U-APM 崩溃分析 |

If vendor has no Google / Umeng login yet, register first, then send the account to owner.

---

## Phase 1 — Owner sends vendor

### Global build

1. Firebase → add Android app with vendor `applicationId`.
2. Download **`google-services.json`**.
3. Firebase → Users and permissions → add vendor **Google email** as **Viewer**.
4. Give vendor:

| File |
|---|
| `crash-kit-global-release.aar` |
| `google-services.json` |
| This guide |

### China build

1. Umeng U-APM → AppKey for this app.
2. Invite vendor **Umeng account** (can open 崩溃分析).
3. Give vendor:

| File / value |
|---|
| `crash-kit-china-release.aar` |
| AppKey string (email or `umeng-appkey.txt`) |
| Channel string if you use one (e.g. `official`) |
| This guide |

---

## Phase 2 — Vendor: Global build

### Files

```text
app/
  google-services.json
  libs/crash-kit-global-release.aar
```

`applicationId` must match the package inside `google-services.json`.

### Gradle

Root:

```kotlin
id("com.google.gms.google-services") version "4.4.2" apply false
id("com.google.firebase.crashlytics") version "3.0.3" apply false
```

App:

```kotlin
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(files("libs/crash-kit-global-release.aar"))
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}
```

### Code (after privacy accept)

```kotlin
import com.bettythegoodboi.crashkit.CrashKit

CrashKit.init(this, enableCollection = true)
CrashKit.setUserId(stableUserId)
CrashKit.log("app started")

try {
    riskyWork()
} catch (e: Exception) {
    CrashKit.recordException(e)
}

// user turns privacy off later:
CrashKit.setCollectionEnabled(false)
```

### Verify

1. Force a fatal crash → **open app again**.
2. Wait a few minutes.
3. Vendor login: **Firebase** → Crashlytics → their package → see the issue.

---

## Phase 3 — Vendor: China build

### Files

```text
app/
  libs/crash-kit-china-release.aar
```

No `google-services.json`. Put AppKey in code or BuildConfig.

### Gradle

```kotlin
dependencies {
    implementation(files("libs/crash-kit-china-release.aar"))
}
```

If Umeng classes missing, add:

```kotlin
implementation("com.umeng.umsdk:common:9.9.1")
implementation("com.umeng.umsdk:asms:1.8.7.2")
implementation("com.umeng.umsdk:apm:2.0.8")
```

### Code (after privacy accept)

```kotlin
CrashKit.init(
    context = this,
    appKey = "APPKEY_FROM_OWNER",
    channel = "official",
    enableCollection = true
)

try {
    riskyWork()
} catch (e: Exception) {
    CrashKit.recordException(e) // console list may need U-APM 专业版
}
```

Privacy off later: do not call CrashKit; next cold start skip `init`.

### Verify

1. Network can reach `https://errnewlog.umeng.com`.
2. Force fatal → **open app again**.
3. Vendor login: **Umeng U-APM** → **崩溃分析** → see the crash.

---

## API

### Global (`crash-kit-global-release.aar`)

| Call | Use |
|---|---|
| `init(context, enableCollection)` | Once after consent |
| `setCollectionEnabled(true/false)` | Privacy on/off |
| `setUserId(id)` | Label in Crashlytics console |
| `log(msg)` | Breadcrumb |
| `setCustomKey(k, v)` | Extra field |
| `recordException(t)` | Non-fatal |

### China (`crash-kit-china-release.aar`)

| Call | Use |
|---|---|
| `init(context, appKey, channel, enableCollection)` | Once after consent |
| `recordException(t)` | Custom exception (console may need U-APM 专业版) |

Uncaught exception = fatal on both.

**China has no `setUserId` / `log` / `setCustomKey` for crash reports.** Do not use those for Umeng.

---

## Checklist

**Vendor → owner**

- [ ] `applicationId`
- [ ] Google email (Global)
- [ ] Umeng account (China)

**Owner → vendor**

- [ ] Matching `.aar`
- [ ] Global: `google-services.json` + Firebase Viewer invite
- [ ] China: AppKey (+ channel) + Umeng invite
- [ ] This guide

**Vendor before ship**

- [ ] Files in paths above
- [ ] `init` only after privacy accept
- [ ] Fatal test visible under **vendor’s** console login
- [ ] One AAR per APK

---

## Failure report to owner

- Global or China  
- `applicationId`  
- App version  
- Fatal tested + app reopened?  
- Screenshot from **your** Firebase or U-APM  
