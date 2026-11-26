package app.couchtv.launcher

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent

/**
 * my-tv: the sleep timer with no computer (option A) that really switches the TV off, also while
 * Netflix, JioHotstar or any other app is playing.
 *
 * The old timer lived in the home's activity and, at the end, only shortened the screen timeout, which
 * every video app overrides while it plays (it keeps the screen on), and the activity could be closed
 * by Android during a long show. This one:
 * - keeps the end time in this app's own storage and counts down in the app process, which stays
 *   alive while the Home button watch (FireWatch, an accessibility service Android keeps bound) is
 *   on; the watch also picks the countdown up again if the process was restarted;
 * - at the end, sends Pause to whatever is playing (AudioManager media key, allowed for any app), so
 *   the show resumes at the right spot, then
 * - asks the Home button watch for Android's "lock screen" action (API 28+), which puts the TV to
 *   sleep: standby, the same as the remote's power button.
 * If the watch is not bound, it falls back to the old way (MainActivity's screensaver timeout).
 */
object SleepTimer {
    private const val PREFS = "couchtv.sleep"
    private const val KEY_END = "end_wall_ms"
    private const val PAUSE_TO_SLEEP_MS = 1500L
    private val main = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null

    /** Called by the page (TV.setSleepTimer). 0 or less cancels. */
    fun set(c: Context, minutes: Int): Boolean {
        val app = c.applicationContext
        val p = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (minutes <= 0) p.edit().remove(KEY_END).apply()
        else p.edit().putLong(KEY_END, System.currentTimeMillis() + minutes * 60_000L).apply()
        schedule(app)
        Log.i("couchtv.sleep", if (minutes > 0) "sleep timer: $minutes min" else "sleep timer: off")
        return true
    }

    /** Whole minutes left, rounded up; 0 when no timer runs. */
    fun remainingMinutes(c: Context): Int {
        val end = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_END, 0L)
        val diff = end - System.currentTimeMillis()
        return if (end <= 0L || diff <= 0L) 0 else ((diff + 59_999L) / 60_000L).toInt()
    }

    /** (Re)arms the countdown from the stored end time. Safe to call any number of times. */
    fun schedule(c: Context) {
        val app = c.applicationContext
        pending?.let { main.removeCallbacks(it) }
        pending = null
        val end = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_END, 0L)
        if (end <= 0L) return
        val r = Runnable { pending = null; fire(app) }
        pending = r
        main.postDelayed(r, (end - System.currentTimeMillis()).coerceAtLeast(0L))
    }

    private fun fire(c: Context) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_END).apply()
        Log.i("couchtv.sleep", "sleep timer ended: pausing, then sleeping the TV")
        runCatching {
            val am = c.getSystemService(AudioManager::class.java)
            val t = SystemClock.uptimeMillis()
            am.dispatchMediaKeyEvent(KeyEvent(t, t, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0))
            am.dispatchMediaKeyEvent(KeyEvent(t, t, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0))
        }.onFailure { Log.w("couchtv.sleep", "pause refused: ${it.message}") }
        main.postDelayed({
            if (!sleepNow()) {
                Log.w("couchtv.sleep", "the Home button watch could not sleep the TV: falling back to the screensaver timeout")
                MainActivity.sleepFallback()
            }
        }, PAUSE_TO_SLEEP_MS)
    }

    /** Puts the TV to sleep through the Home button watch. False when that is not possible here. */
    private fun sleepNow(): Boolean {
        val s = FireWatch.instance ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return runCatching { s.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) }.getOrDefault(false)
    }
}
