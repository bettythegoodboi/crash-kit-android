# CrashKit — Vendor Integration Guide

**API package:** `com.bettythegoodboi.crashkit.CrashKit`  
**Choose one AAR per product binary — do not mix Firebase and Umeng in the same APK.**

| Region | AAR | Backend |
|---|---|---|
| **Global** (outside China) | `crash-kit-global-release.aar` | Firebase Crashlytics |
| **China** | `crash-kit-china-release.aar` | Umeng U-APM |

Owner provides the matching config (json **or** AppKey). Crash data goes to **owner’s** project unless agreed otherwise.

---

## Overview — who does what

| Role | Does |
|---|---|
| **Owner** | Owns Firebase and/or Umeng; registers vendor package / issues AppKey; sends AAR + config + this doc; optional console Viewer |
| **Vendor** | Sends `applicationId` (+ Google email for Firebase Viewer); integrates **one** AAR + config; calls CrashKit after privacy consent; ships app |

---

## Phase 0 — Vendor → Owner (kickoff)

Vendor sends:

1. **Android `applicationId`** (final package name)
2. **Target region(s):** Global, China, or both (two product flavors / two APKs)
3. **Google account email** (optional) — only if vendor needs **Firebase** console Viewer
4. App display name (optional)

---

## Phase 1A — Owner setup (Global / Firebase)

1. Firebase Console → owner project → **Add Android app** with vendor `applicationId`.
2. Download **`google-services.json`** for that package.
3. Optional: Project settings → Users → add vendor Google email as **Viewer**.
4. Send: `crash-kit-global-release.aar` + `google-services.json` + this doc.

---

## Phase 1B — Owner setup (China / Umeng)

1. Umeng U-APM → create or select app → copy **AppKey**.
2. Note: U-APM is China-oriented; devices must reach `errnewlog.umeng.com`.
3. Free tier: **fatal crashes** in 崩溃分析. **自定义异常** console may require 专业版.
4. Send: `crash-kit-china-release.aar` + **AppKey** (+ channel string if required) + this doc.

---

## Phase 2 — Vendor: Global integration

### 2.1 Package + json

`applicationId` must match `google-services.json`.

Place file at:

```text
app/google-services.json
```

### 2.2 AAR + plugins

```text
app/libs/crash-kit-global-release.aar
```

Root plugins (versions as needed):

```kotlin
id("com.google.gms.google-services") version "4.4.2" apply false
id("com.google.firebase.crashlytics") version "3.0.3" apply false
```

App module:

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

### 2.3 Init (after privacy consent)

```kotlin
import com.bettythegoodboi.crashkit.CrashKit

CrashKit.init(this, enableCollection = true)
CrashKit.setUserId(userId)
CrashKit.log("app started")
```

### 2.4 API (global)

| Method | Purpose |
|---|---|
| `init(context, enableCollection)` | Required once |
| `setCollectionEnabled(boolean)` | Toggle reporting |
| `setUserId(String)` | Label reports |
| `setCustomKey(...)` | Extra fields |
| `log(String)` | Breadcrumb |
| `recordException(Throwable)` | Non-fatal |

Fatal crashes are automatic (uncaught). Reopen app after fatal for upload.

---

## Phase 3 — Vendor: China integration

### 3.1 AAR only (no google-services)

```text
app/libs/crash-kit-china-release.aar
```

```kotlin
dependencies {
    implementation(files("libs/crash-kit-china-release.aar"))
    // Umeng is api()-exposed by the AAR; if resolve fails, add:
    // implementation("com.umeng.umsdk:common:9.9.1")
    // implementation("com.umeng.umsdk:asms:1.8.7.2")
    // implementation("com.umeng.umsdk:apm:2.0.8")
}
```

Maven Central required for Umeng artifacts.

### 3.2 Init (after privacy consent)

```kotlin
CrashKit.init(
    context = this,
    appKey = "YOUR_UMENG_APPKEY",  // from owner
    channel = "official",          // or owner-provided channel
    enableCollection = true
)
CrashKit.setUserId(userId)
```

### 3.3 API (china)

| Method | Purpose |
|---|---|
| `init(context, appKey, channel, enableCollection)` | Required once |
| `recordException(Throwable)` | Custom exception (`UMCrash.generateCustomLog`) |
| `setUserId` | Best-effort |
| `log` / `setCustomKey` | No-op or limited on free U-APM |

Fatal: uncaught exceptions. **Reopen app** after fatal. Network must reach Umeng.

Verify: U-APM → **崩溃分析**. 自定义异常 may need paid plan.

---

## Phase 4 — Dual region product (recommended)

Ship **two application variants** (product flavors):

| Flavor | Depends on | Config |
|---|---|---|
| `global` | global AAR | `google-services.json` |
| `china` | china AAR | Umeng AppKey |

Same call sites in app code where possible; `init` differs by flavor source set.

---

## Phase 5 — Verify

### Global

1. Debug install, network on  
2. Force uncaught crash → **reopen**  
3. Firebase → Crashlytics → vendor package → issue appears  

### China

1. Network can reach `https://errnewlog.umeng.com`  
2. Force fatal → **reopen**  
3. U-APM → app → **崩溃分析**  

---

## Checklist

### Owner

- [ ] Vendor `applicationId` + region(s)
- [ ] Global: Firebase app + json (optional Viewer)
- [ ] China: Umeng AppKey (+ channel)
- [ ] Sent correct AAR(s) + config + this doc

### Vendor

- [ ] One AAR per binary (no mix)
- [ ] Config matches package / AppKey
- [ ] `CrashKit.init` after consent
- [ ] Fatal test verified in owner console
- [ ] No test-only crash UI in production release

---

## Support info to include

- Region (global / china)
- `applicationId` / AppKey used
- App version
- Fatal vs non-fatal
- Console screenshot or issue id
