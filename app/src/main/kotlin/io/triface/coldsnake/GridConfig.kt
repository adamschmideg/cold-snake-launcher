package io.triface.coldsnake

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * The three spare grid slots (beyond the fixed Phone/Messages/Camera/Clock/
 * Calendar tiles) unlock progressively as Ice Time and Discipline grow, and
 * each tier restricts which apps can fill the slot:
 *
 * - Tier 1 (30 min Ice Time, 80%+ Discipline): notes/to-do apps only. There's
 *   no Android role category for this, so it's a small hardcoded allowlist
 *   of apps vetted to stay bounded (no feeds, no discovery).
 * - Tier 2 (60 min): tier 1's list plus Email/Weather/Music — real Android
 *   app-role categories, still bounded by construction.
 * - Tier 3 (90 min): fully open — any installed, launchable app. Earned
 *   trust after sustained, consistent use.
 */
object GridConfig {
    private const val PREFS_NAME = "cold_snake_grid_config"
    private const val KEY_SLOT_PREFIX = "custom_slot_"

    const val SLOT_COUNT = 3
    private const val DISCIPLINE_THRESHOLD_PERCENT = 80
    private val ICE_TIME_THRESHOLDS_SECONDS = listOf(30 * 60, 60 * 60, 90 * 60)

    // Vetted notes/to-do apps: self-contained task lists, no feed or discovery
    // surface. Extend this list only with apps that keep the same property.
    private val NOTES_TODO_PACKAGES = listOf(
        "com.google.android.apps.tasks",
        "com.google.android.keep",
        "com.todoist",
        "com.ticktick.task",
        "com.samsung.android.app.notes",
    )

    private val TIER_2_ROLE_CATEGORIES = listOf(
        Intent.CATEGORY_APP_EMAIL,
        Intent.CATEGORY_APP_WEATHER,
        Intent.CATEGORY_APP_MUSIC,
    )

    /** How many of the 3 custom slots are unlocked, based on current stats. */
    fun unlockedSlotCount(context: Context): Int {
        val disciplinePercent = Stats.disciplinePercent(context) ?: 0
        if (disciplinePercent < DISCIPLINE_THRESHOLD_PERCENT) return 0

        val iceTimeSeconds = Stats.iceTimeSeconds(context)
        return ICE_TIME_THRESHOLDS_SECONDS.count { iceTimeSeconds >= it }
    }

    /** 1-indexed: minutes of Ice Time required to unlock the given slot. */
    fun iceTimeThresholdMinutes(slotIndex: Int): Int =
        ICE_TIME_THRESHOLDS_SECONDS[slotIndex - 1] / 60

    fun disciplineThresholdPercent(): Int = DISCIPLINE_THRESHOLD_PERCENT

    fun getSlotPackage(context: Context, slotIndex: Int): String? =
        prefs(context).getString(KEY_SLOT_PREFIX + slotIndex, null)

    fun setSlotPackage(context: Context, slotIndex: Int, packageName: String) {
        prefs(context).edit().putString(KEY_SLOT_PREFIX + slotIndex, packageName).apply()
    }

    /** All package names currently assigned to a custom slot, for the notification allowlist. */
    fun assignedPackages(context: Context): Set<String> =
        (1..SLOT_COUNT).mapNotNull { getSlotPackage(context, it) }.toSet()

    /** Installed, launchable apps eligible for the given (1-indexed) slot's tier. */
    fun candidatesForSlot(context: Context, slotIndex: Int): List<ApplicationInfo> {
        val pm = context.packageManager
        return when (slotIndex) {
            1 -> resolvePackages(pm, NOTES_TODO_PACKAGES)
            2 -> resolvePackages(pm, NOTES_TODO_PACKAGES) + resolveByCategories(pm, TIER_2_ROLE_CATEGORIES)
            else -> allLaunchableApps(pm, excludingPackage = context.packageName)
        }.distinctBy { it.packageName }
    }

    private fun resolvePackages(pm: PackageManager, packageNames: List<String>): List<ApplicationInfo> =
        packageNames.mapNotNull { pkg ->
            if (pm.getLaunchIntentForPackage(pkg) != null) {
                runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull()
            } else {
                null
            }
        }

    private fun resolveByCategories(pm: PackageManager, categories: List<String>): List<ApplicationInfo> =
        categories.flatMap { category ->
            val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
            pm.queryIntentActivities(intent, 0).map { it.activityInfo.applicationInfo }
        }

    private fun allLaunchableApps(pm: PackageManager, excludingPackage: String): List<ApplicationInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.applicationInfo }
            .filter { it.packageName != excludingPackage }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
