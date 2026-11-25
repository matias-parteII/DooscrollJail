package com.doomscrolljail.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.doomscrolljail.app.data.BlockerPrefs

class ShortFormBlockerService : AccessibilityService() {

    private lateinit var prefs: BlockerPrefs
    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastChatContextTimestamp: Long = 0L
    private var chatGraceSessionActive: Boolean = false
    private var chatGracePackage: String? = null
    private var chatGraceStartedAt: Long = 0L
    private var lastBlockActionTimestamp: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = BlockerPrefs(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!::prefs.isInitialized) prefs = BlockerPrefs(this)

        val pkg = event.packageName?.toString() ?: return
        val now = System.currentTimeMillis()

        if (pkg == PKG_SETTINGS && prefs.settingsTamperGuard && !prefs.isCurrentlyUnlocked()) {
            val root = rootInActiveWindow
            if (root != null && isViewingOurAccessibilityPage(root)) {
                triggerBlock(
                    reason = "Nice try. Complete The Gauntlet inside DoomscrollJail first.",
                    now = now
                )
                return
            }
        }

        if (prefs.allowChatVideos) {
            if (pkg in EXTERNAL_MESSENGER_PACKAGES) {
                lastChatContextTimestamp = now
                resetChatGraceSession()
                return
            }
        }

        if (pkg !in TARGET_PACKAGES) {
            if (chatGraceSessionActive && pkg != chatGracePackage && pkg != PKG_SYSTEM_UI) {
                resetChatGraceSession()
            }
            return
        }

        val root = rootInActiveWindow ?: return

        if (prefs.allowChatVideos && isInAppChatScreen(pkg, root, event)) {
            lastChatContextTimestamp = now
            resetChatGraceSession()
            return
        }

        val isShortFormFeed = when (pkg) {
            PKG_YOUTUBE -> prefs.blockShorts && isYouTubeShortsScreen(root)
            PKG_INSTAGRAM -> prefs.blockReels && isInstagramReelsScreen(root)
            PKG_TIKTOK, PKG_TIKTOK_LITE -> prefs.blockTikTok && isTikTokFeedScreen(root)
            else -> false
        }

        if (!isShortFormFeed) {
            if (chatGraceSessionActive && pkg == chatGracePackage) {
                resetChatGraceSession()
            }
            return
        }

        if (prefs.allowChatVideos) {
            if (chatGraceSessionActive && pkg == chatGracePackage) {
                if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                    resetChatGraceSession()
                    triggerBlock("Chat video finished — no scrolling the feed!", now)
                    return
                }
                if (now - chatGraceStartedAt > MAX_SINGLE_CHAT_VIDEO_MS) {
                    resetChatGraceSession()
                    triggerBlock("Chat video time limit reached.", now)
                    return
                }
                return
            }

            if (now - lastChatContextTimestamp <= CHAT_TRANSITION_WINDOW_MS) {
                chatGraceSessionActive = true
                chatGracePackage = pkg
                chatGraceStartedAt = now
                lastChatContextTimestamp = 0L
                prefs.incrementChatAllowedCount()
                showToast("Chat video allowed (1 video only — don't swipe!)")
                return
            }
        }

        val label = when (pkg) {
            PKG_YOUTUBE -> "YouTube Shorts blocked"
            PKG_INSTAGRAM -> "Instagram Reels blocked"
            else -> "TikTok feed blocked"
        }
        triggerBlock(label, now)
    }

    private fun triggerBlock(reason: String, now: Long) {
        if (now - lastBlockActionTimestamp < BLOCK_DEBOUNCE_MS) return
        lastBlockActionTimestamp = now
        prefs.incrementBlockedCount()
        performGlobalAction(GLOBAL_ACTION_BACK)
        showToast(reason)
    }

    private fun resetChatGraceSession() {
        chatGraceSessionActive = false
        chatGracePackage = null
        chatGraceStartedAt = 0L
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isViewingOurAccessibilityPage(root: AccessibilityNodeInfo): Boolean {
        val hasAppName = findNodeByText(root, "DoomscrollJail", exact = false) != null
        val hasShortcutOrUse = findNodeByText(root, "Use DoomscrollJail", exact = false) != null ||
            findNodeByText(root, "DoomscrollJail shortcut", exact = false) != null ||
            findNodeByText(root, "Accessibility", exact = false) != null
        return hasAppName && hasShortcutOrUse
    }

    private fun isInAppChatScreen(
        pkg: String,
        root: AccessibilityNodeInfo,
        event: AccessibilityEvent
    ): Boolean {
        val className = event.className?.toString()?.lowercase().orEmpty()
        if (className.contains("direct") || className.contains("chat") || className.contains("inbox")) {
            return true
        }

        return when (pkg) {
            PKG_INSTAGRAM -> {
                hasAnyViewId(
                    root,
                    listOf(
                        "com.instagram.android:id/direct_inbox_null_state",
                        "com.instagram.android:id/direct_thread_recycler_view",
                        "com.instagram.android:id/row_thread_composer_edittext",
                        "com.instagram.android:id/direct_inbox_search_edit_text"
                    )
                ) || findNodeByText(root, "Message...", exact = false) != null
            }
            PKG_TIKTOK, PKG_TIKTOK_LITE -> {
                findNodeByText(root, "Send a message...", exact = false) != null ||
                    findNodeByText(root, "Direct messages", exact = false) != null ||
                    findNodeByText(root, "All activity", exact = false) != null
            }
            PKG_YOUTUBE -> {
                findNodeByText(root, "Send in YouTube", exact = false) != null
            }
            else -> false
        }
    }

    private fun isYouTubeShortsScreen(root: AccessibilityNodeInfo): Boolean {
        return hasAnyViewId(
            root,
            listOf(
                "com.google.android.youtube:id/reel_watch_fragment_root",
                "com.google.android.youtube:id/reel_recycler",
                "com.google.android.youtube:id/shorts_player_container",
                "com.google.android.youtube:id/reel_player_page_container"
            )
        )
    }

    private fun isInstagramReelsScreen(root: AccessibilityNodeInfo): Boolean {
        return hasAnyViewId(
            root,
            listOf(
                "com.instagram.android:id/clips_viewer_view_pager",
                "com.instagram.android:id/clips_video_container",
                "com.instagram.android:id/reel_viewer_root"
            )
        )
    }

    private fun isTikTokFeedScreen(root: AccessibilityNodeInfo): Boolean {
        val onForYouOrFollowing =
            findNodeByText(root, "For You", exact = true) != null ||
                findNodeByText(root, "Following", exact = true) != null

        val hasVideoPager = hasAnyViewId(
            root,
            listOf(
                "com.zhiliaoapp.musically:id/viewpager",
                "com.ss.android.ugc.trill:id/viewpager"
            )
        )

        val inSafeTab =
            findNodeByText(root, "Send a message...", exact = false) != null ||
                findNodeByText(root, "Edit profile", exact = true) != null

        return !inSafeTab && (onForYouOrFollowing || hasVideoPager)
    }

    private fun hasAnyViewId(root: AccessibilityNodeInfo, ids: List<String>): Boolean {
        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                return true
            }
        }
        return false
    }

    private fun findNodeByText(
        root: AccessibilityNodeInfo,
        query: String,
        exact: Boolean
    ): AccessibilityNodeInfo? {
        val matches = root.findAccessibilityNodeInfosByText(query) ?: return null
        for (node in matches) {
            val txt = node.text?.toString() ?: continue
            if (exact && txt.equals(query, ignoreCase = true)) return node
            if (!exact && txt.contains(query, ignoreCase = true)) return node
        }
        return null
    }

    override fun onInterrupt() {
    }

    companion object {
        private const val PKG_YOUTUBE = "com.google.android.youtube"
        private const val PKG_INSTAGRAM = "com.instagram.android"
        private const val PKG_TIKTOK = "com.zhiliaoapp.musically"
        private const val PKG_TIKTOK_LITE = "com.ss.android.ugc.trill"
        private const val PKG_SETTINGS = "com.android.settings"
        private const val PKG_SYSTEM_UI = "com.android.systemui"

        private val TARGET_PACKAGES = setOf(
            PKG_YOUTUBE,
            PKG_INSTAGRAM,
            PKG_TIKTOK,
            PKG_TIKTOK_LITE
        )

        private val EXTERNAL_MESSENGER_PACKAGES = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "org.thunderdog.challegram",
            "com.discord",
            "com.facebook.orca",
            "com.google.android.apps.messaging",
            "com.snapchat.android"
        )

        private const val CHAT_TRANSITION_WINDOW_MS = 8_000L
        private const val MAX_SINGLE_CHAT_VIDEO_MS = 180_000L
        private const val BLOCK_DEBOUNCE_MS = 650L
    }
}
