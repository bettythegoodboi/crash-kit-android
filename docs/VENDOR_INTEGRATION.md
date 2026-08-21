# CrashKit — Vendor Integration Guide

Read in order. Do not skip console access.

**Library class:** `com.bettythegoodboi.crashkit.CrashKit`  
**Rule:** One region = one AAR. Do **not** put Firebase and Umeng in the same APK.

| Region | File vendor must put in the app | Crash backend |
|---|---|---|
| Global | `crash-kit-global-release.aar` | Firebase Crashlytics |
| China | `crash-kit-china-release.aar` | Umeng U-APM |

Crash reports go to the **owner’s** Firebase / Umeng project. Vendor **must** get console access (see Phase 0).

---

## Phase 0 — Required accounts (vendor → owner)

Vendor **must** register and send these to the owner. Console access is **required**, not optional.

### Always send

| # | Item | Example |
|---|---|---|
| 1 | Android package name (`applicationId`) | `com.vendor.product` |
| 2 | Region(s) to ship | Global only / China only / both |

### Global (Firebase) — required for console

| # | Item | Rules |
|---|---|---|
| 3 | **Google account email** | Gmail or Google Workspace only. Vendor creates/uses this account. Owner adds it to Firebase so vendor can open Crashlytics. |

### China (Umeng) — required for console

| # | Item | Rules |
|---|---|---|
| 4 | **Umeng account email / login** used by vendor | Vendor registers at [Umeng](https://www.umeng.com/) if needed. Owner invites this account into the Umeng app/project so vendor can open U-APM 崩溃分析. |

Owner will not finish handoff until package name + the matching console account(s) are received.

---

## Phase 1 — Owner prepares files

### Global

1. Firebase → owner project → Add Android app → package name = vendor `applicationId`.
2. Download file: **`google-services.json`**.
3. Firebase → Project settings → Users and permissions → Add member → vendor **Google email** → role **Viewer** (or higher if agreed).
4. Send vendor this package:

| File | What it is |
|---|---|
| `crash-kit-global-release.aar` | Library |
| `google-services.json` | Firebase binding for **their** package name |
| `VENDOR_INTEGRATION.md` | This guide |

### China

1. Umeng U-APM → app for this product → copy **AppKey** (string).
2. Invite vendor **Umeng account** into that app/project (member access so they can open 崩溃分析).
3. Send vendor this package:

| File / value | What it is |
|---|---|
| `crash-kit-china-release.aar` | Library |
| AppKey string (e.g. in email or `umeng-appkey.txt`) | e.g. `6a71ead6934d206f5852c9ab` |
| Channel string (if used) | e.g. `official` or `demo` |
| `VENDOR_INTEGRATION.md` | This guide |

---

## Phase 2 — Vendor: Global app

### 2.1 Place files

```text
your-app/
  app/
    google-services.json          ← from owner, do not rename
    libs/
      crash-kit-global-release.aar
```

In `app/build.gradle.kts`, `applicationId` **must** equal the package inside `google-services.json`.

### 2.2 Gradle

Root `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
}
```

App `app/build.gradle.kts`:

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

### 2.3 Code (after user accepts privacy policy)

```kotlin
import com.bettythegoodboi.crashkit.CrashKit

// Application.onCreate or right after consent
CrashKit.init(this, enableCollection = true)
CrashKit.setUserId(stableUserId)

// later
CrashKit.log("opened checkout")
try {
    riskyWork()
} catch (e: Exception) {
    CrashKit.recordException(e)
}

// if user turns privacy OFF later
CrashKit.setCollectionEnabled(false)
```

### 2.4 Check it worked

1. Install debug build, network on.
2. Trigger a test fatal crash (uncaught exception).
3. **Open the app again** (upload is usually on next launch).
4. Wait a few minutes.
5. Vendor logs into **Firebase Console** with the Google account from Phase 0 → Crashlytics → select **their package** → see the issue.

---

## Phase 3 — Vendor: China app

### 3.1 Place files

```text
your-app/
  app/
    libs/
      crash-kit-china-release.aar
```

No `google-services.json` for China.

Keep AppKey from owner in code or BuildConfig (not a Firebase file).

### 3.2 Gradle

```kotlin
dependencies {
    implementation(files("libs/crash-kit-china-release.aar"))
}
```

Project must resolve Maven Central. If Umeng classes are missing, add:

```kotlin
implementation("com.umeng.umsdk:common:9.9.1")
implementation("com.umeng.umsdk:asms:1.8.7.2")
implementation("com.umeng.umsdk:apm:2.0.8")
```

### 3.3 Code (after user accepts privacy policy)

```kotlin
import com.bettythegoodboi.crashkit.CrashKit

CrashKit.init(
    context = this,
    appKey = "PASTE_APPKEY_FROM_OWNER",
    channel = "official",   // or string owner gave you
    enableCollection = true
)
CrashKit.setUserId(stableUserId)

try {
    riskyWork()
} catch (e: Exception) {
    CrashKit.recordException(e)  // console list may need U-APM 专业版
}
```

If user turns privacy OFF: do not call CrashKit; on next cold start do not call `init`.

### 3.4 Check it worked

1. Phone can open `https://errnewlog.umeng.com` (China-oriented network).
2. Trigger fatal crash → **open app again**.
3. Wait several minutes.
4. Vendor logs into **Umeng U-APM** with the account from Phase 0 → select the app → **崩溃分析** → see the crash.

---

## Phase 4 — Both regions in one product

Build **two** variants (product flavors or two apps):

| Variant | AAR file | Other file / value |
|---|---|---|
| global | `crash-kit-global-release.aar` | `app/google-services.json` |
| china | `crash-kit-china-release.aar` | AppKey string from owner |

---

## API summary

### Global AAR

| Call | Meaning |
|---|---|
| `CrashKit.init(context, enableCollection)` | Start; call once after consent |
| `CrashKit.setCollectionEnabled(false/true)` | Stop or resume upload |
| `CrashKit.setUserId(id)` | Label in console |
| `CrashKit.log(msg)` | Breadcrumb |
| `CrashKit.setCustomKey(k, v)` | Extra field |
| `CrashKit.recordException(t)` | Non-fatal |

### China AAR

| Call | Meaning |
|---|---|
| `CrashKit.init(context, appKey, channel, enableCollection)` | Start; call once after consent |
| `CrashKit.recordException(t)` | Custom exception (view may need paid U-APM) |
| `CrashKit.setUserId(id)` | Best-effort |

Uncaught exceptions = fatal reports on both.

---

## Checklist

### Vendor sends owner

- [ ] `applicationId`
- [ ] Region(s)
- [ ] **Google email** (if Global) — for Firebase access
- [ ] **Umeng account** (if China) — for U-APM access

### Owner sends vendor

- [ ] Correct `.aar` file(s)
- [ ] Global: `google-services.json`
- [ ] China: AppKey (+ channel if any)
- [ ] This guide
- [ ] Console invite done (Firebase Viewer / Umeng member)

### Vendor before release

- [ ] Files in the paths above
- [ ] `CrashKit.init` only after privacy accept
- [ ] Fatal test visible in **vendor’s own** console login
- [ ] No mix of both AARs in one APK

---

## If something fails, send owner

- Region: global or china  
- `applicationId`  
- App version name / code  
- Whether fatal was tested and app was reopened  
- Screenshot from **your** Firebase or U-APM login  
