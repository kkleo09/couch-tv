package app.couchtv.launcher

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * my-tv: the TV's memory, for the home's Memory button, Home settings, About, and the low-memory
 * question; and the TV's own power menu (to restart it), which is what those offer.
 *
 * Google TV has no memory cleaner of its own (the phone ones are Samsung's, Xiaomi's and the like), and
 * on this Sony (Android 12) no app may close other apps: ActivityManager.killBackgroundProcesses logs
 * "Invalid packageName" for every app but our own, the rule standard Android only added in 14. A restart
 * frees the most memory anyway, so the home offers that, through the TV's own power menu.
 */
object MemoryTools {
    /** JSON in kB: total, avail (what apps can get soon), free (not used at all), swap_total, swap_free, low. */
    fun info(c: Context): String {
        val am = c.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val proc = readMeminfo()
        return JSONObject()
            .put("total", mi.totalMem / 1024)
            .put("avail", mi.availMem / 1024)
            .put("free", proc["MemFree"] ?: -1L)
            .put("swap_total", proc["SwapTotal"] ?: 0L)
            .put("swap_free", proc["SwapFree"] ?: 0L)
            .put("low", mi.lowMemory)
            .toString()
    }

    /** Opens the TV's power menu (Restart, Power off) through the Home button watch. False without the watch. */
    fun powerMenu(): Boolean {
        val s = FireWatch.instance ?: return false
        return runCatching { s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) }.getOrDefault(false)
    }

    private fun readMeminfo(): Map<String, Long> = runCatching {
        File("/proc/meminfo").readLines().mapNotNull { line ->
            val parts = line.split(Regex("[:\\s]+")).filter { it.isNotEmpty() }
            if (parts.size >= 2) parts[0] to (parts[1].toLongOrNull() ?: return@mapNotNull null) else null
        }.toMap()
    }.getOrDefault(emptyMap())
}
