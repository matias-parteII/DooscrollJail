package com.doomscrolljail.app.data

import android.content.Context
import android.content.SharedPreferences

class BlockerPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("doomscroll_jail_prefs", Context.MODE_PRIVATE)

    var blockShorts: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SHORTS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_SHORTS, value).apply()

    var blockReels: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_REELS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_REELS, value).apply()

    var blockTikTok: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_TIKTOK, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_TIKTOK, value).apply()

    var allowChatVideos: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_CHAT_VIDEOS, true)
        set(value) = prefs.edit().putBoolean(KEY_ALLOW_CHAT_VIDEOS, value).apply()

    var settingsTamperGuard: Boolean
        get() = prefs.getBoolean(KEY_SETTINGS_TAMPER_GUARD, true)
        set(value) = prefs.edit().putBoolean(KEY_SETTINGS_TAMPER_GUARD, value).apply()

    var unlockedUntilMillis: Long
        get() = prefs.getLong(KEY_UNLOCKED_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_UNLOCKED_UNTIL, value).apply()

    var blockedCount: Int
        get() = prefs.getInt(KEY_BLOCKED_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_BLOCKED_COUNT, value).apply()

    var chatAllowedCount: Int
        get() = prefs.getInt(KEY_CHAT_ALLOWED_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_CHAT_ALLOWED_COUNT, value).apply()

    fun isCurrentlyUnlocked(): Boolean {
        return System.currentTimeMillis() < unlockedUntilMillis
    }

    fun grantTemporaryUnlock(durationMillis: Long = 5 * 60 * 1000L) {
        unlockedUntilMillis = System.currentTimeMillis() + durationMillis
    }

    fun relockImmediately() {
        unlockedUntilMillis = 0L
    }

    fun incrementBlockedCount() {
        blockedCount += 1
    }

    fun incrementChatAllowedCount() {
        chatAllowedCount += 1
    }

    companion object {
        private const val KEY_BLOCK_SHORTS = "block_shorts"
        private const val KEY_BLOCK_REELS = "block_reels"
        private const val KEY_BLOCK_TIKTOK = "block_tiktok"
        private const val KEY_ALLOW_CHAT_VIDEOS = "allow_chat_videos"
        private const val KEY_SETTINGS_TAMPER_GUARD = "settings_tamper_guard"
        private const val KEY_UNLOCKED_UNTIL = "unlocked_until_millis"
        private const val KEY_BLOCKED_COUNT = "blocked_count"
        private const val KEY_CHAT_ALLOWED_COUNT = "chat_allowed_count"
    }
}
