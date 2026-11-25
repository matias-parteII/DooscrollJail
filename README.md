# DoomscrollJail (Anti-Short-Form Video Blocker for Android)

**DoomscrollJail** is a native Android app (Kotlin + Jetpack Compose) that uses Android's `AccessibilityService` to detect and immediately exit addictive short-form video feeds (**YouTube Shorts**, **Instagram Reels**, and **TikTok**), while **allowing videos opened directly from in-app chat / DMs**.

To stop impulsive disabling, turning off the blocker (or pausing it) requires surviving **The Gauntlet**: a 12-stage interrogation of passive-aggressive confirmation prompts with enforced cooldown timers (escalating from 6 seconds up to 20 seconds per question), typing challenges, and alternating button positions.

---

## Core Features

1. **Feed-Level Short-Form Blocking**
   - **YouTube Shorts**: Detects `reel_watch_fragment_root` / `shorts_player` containers and presses `GLOBAL_ACTION_BACK`.
   - **Instagram Reels**: Detects Reels viewer view IDs (`clips_viewer_view_pager`, `reel_viewer_root`) and backs out.
   - **TikTok**: Blocks the endless *For You* / *Following* / *Friends* video feed (`com.zhiliaoapp.musically` / `com.ss.android.ugc.trill`) while leaving the **Inbox / Direct Messages** tabs accessible.
2. **Chat / DM Exception Heuristic**
   - Tracks recent UI navigation context via `AccessibilityEvent` history.
   - If you were inside a DM / Chat screen (e.g., Instagram Direct, TikTok Inbox/Chat, WhatsApp, Telegram, Discord, Messages, or YouTube Share/Chat) within the last **8 seconds** before opening a Reel/Short/TikTok, DoomscrollJail grants a **single-video grace window** so you can watch what your friend sent you.
   - As soon as you swipe to the next video or exceed the grace session, the blocker snaps shut again.
3. **The Deactivation Gauntlet (Anti-Impulse Friction)**
   - Attempting to disable blocking or pause protection triggers a **12-question interrogation** (*"Are you sure?"*, *"Are you 100% sure?"*, *"Are you 1000% sure? Really?"*, typing tests, etc.).
   - Every single step has a mandatory **countdown lock** (6s → 20s) before the "Yes, disable" button becomes clickable.
   - If you leave the app or background it mid-gauntlet, **progress resets to Question 1**.
   - Also monitors Android's **System Accessibility Settings** screen: if you navigate to DoomscrollJail's system toggle while protection is armed, it intercepts and bounces you back unless you unlocked a 5-minute maintenance window via The Gauntlet first.

---

## Setup & Installation in Android Studio

1. **Unzip** `DoomscrollJail.zip` and open the `DoomscrollJail` folder in **Android Studio**.
2. Let Gradle sync and download dependencies.
3. Connect an Android device (Android 8.0 / API 26+) with **USB Debugging** enabled, or launch an emulator.
4. Click **Run (`▶`)** to install the app.
5. Inside **DoomscrollJail**:
   - Tap **Enable Accessibility Service** → Find **DoomscrollJail** under *Installed Apps* → Toggle **ON**.
   - (Android 13+ sideload note): If Android grays out the Accessibility switch with *"Restricted setting"*, open **Settings → Apps → DoomscrollJail → ⋮ (top right) → Allow restricted settings**, then return to Accessibility settings.
6. Arm the toggles for **YouTube Shorts**, **Instagram Reels**, and **TikTok**, and enable **Settings Tamper Guard**.
