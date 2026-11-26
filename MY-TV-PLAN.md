# My couch-tv plan (Sony Bravia Google TV)

Written 17 November 2026 from the walkthrough with Claude. Nothing has been changed on the TV yet.

> **Private notes.** This file has names, the suburb and the TV's network details. Keep it out of a
> public repo (add it to `.gitignore`, or keep the fork private).

## Status (17 November 2026)

- **Installed:** the home app built from **main** (no custom code yet), with `config.json` built in,
  via `apply.sh` (option A, `PRIVACY=1`). The 0.12 release app was tried first, then removed: it
  lacked the Jio tile, Follow the sun, weather effects, the sleep and TV settings buttons and fading rows.
- **Switched off** (undo list `tv-home/tvs/living-room.disabled`): `dreamx` (ambient mode),
  `com.google.android.feedback`, `tv.samba.ssm`, `com.sony.dtv.promos`, `com.sony.dtv.sonyselect`,
  `com.sony.dtv.notificationcenter`. Google TV home and setup stay on.
- **Seen working on the TV:** Waterline scene at midday (Follow the sun), Kakkanad weather, liquid clock
  with no AM/PM, Input, TV settings and Sleep quick buttons, the Jio tile (near the end of the app row),
  Home button brings couch-tv back.
- **Build tools** (inside `build/`, nothing system-wide): Temurin JDK 21, Android SDK (platform 34,
  build tools 34; the Android SDK licence was accepted with the user's OK).
  Rebuild: `JAVA_HOME=build/jdk/jdk-21.0.13+9/Contents/Home ANDROID_HOME=build/android-sdk sh tv-home/android/gradlew -p tv-home/android --no-daemon -q clean assembleRelease -PconfigJson=$PWD/config.json`
  (use absolute paths), then copy `tv-home/android/app/build/outputs/apk/release/app-release.apk` to `dist/couch-tv-home.apk`.
- **Signing key:** the build is signed with this Mac's `~/.android/debug.keystore`. Later builds from
  the same key install over it and keep the TV's settings and app order. Back the keystore up; a
  build with another key means removing the app first (settings on the TV start fresh).
- **App row (arranged from the Mac, 17 Nov):** Jio → JioHotstar → YouTube → Sony LIV → Prime Video →
  Netflix → Zee5, then Settings and More apps. Everything else is behind More apps. The Jio tile was
  tested: it switches the TV to HDMI 1.
- **Tip:** holding OK cannot be sent from the Mac, but Home settings → Apps works with plain OK:
  each OK moves an app On home → More apps → hidden → back On home **at the end of the row**.
- **Custom code (installed 17 Nov, carry to the `my-tv` branch of the fork):**
  1. **Three names side by side** at the top left (`tv-home/web/app.js`, `isHead()`: round icon only from 4 people).
  2. **Per-person Keep watching** for option A (`tv-home/web/app.js`, the "my-tv: per-person Keep
     watching" block, `refresh()` and the card hold menu; `bridge/config.py` passes
     `home.keep_watching_owners`). Who owns a card: a "Move to" made with the remote (hold OK on the
     card) wins, then the `keep_watching_owners` list in `config.json` (matched by whole words in
     the title), then the person picked when the card first appeared. To change the list: edit
     `config.json`, rebuild, run `apply.sh`.
  Checked on the TV: Anirudh = Pokémon, Cars, Sonic, Minions & Monsters; Karthik = Lord of the Rings,
  Daayra, Papa Yaar, Shark Tank, Sapne vs Everyone; Anija = Good Witch.
  3. **Sleep timer that really switches off** (new `SleepTimer.kt`, small hooks in `FireWatch.kt` and
     `MainActivity.kt`): at the end it sends Pause to whatever plays, then the Home button watch puts
     the TV to sleep (standby). Tested 17 Nov during Netflix: the TV log shows the pause sent to
     Netflix, then "Going to sleep due to accessibility" 1.5 s later.
  Backups in `dist/`: `couch-tv-home-before-sleep.apk` (before change 3), `couch-tv-home-0.12.apk` (release), `couch-tv-home-main-plain.apk` (main, no custom code).
- **Still to do:** Sony privacy menu switches; FanCode when installed.

## The TV and the house

| | |
|---|---|
| TV | Sony Bravia, Google TV |
| TV address | 192.168.29.202 (reserve it in the router: likely Jio Fiber, `http://192.168.29.1`, LAN/DHCP settings) |
| TV MAC | (not stored here: read it on the TV, Settings → Network) (only for the router reservation, not for couch-tv) |
| Jio set-top box | HDMI 1 |
| Computer that stays on | None, so option A |

**Who watches:**
- **Karthik** (main user): football (Manchester United), cricket (RCB), series and films on Netflix, Prime Video, JioHotstar.
- **Anirudh** (kid): chess (Lichess, Chess.com), cartoons (Pokémon, Peppa Pig, Grizzy and the Lemmings, Ben & Holly's Little Kingdom, Tom & Jerry, Bluey, TD Bricks on YouTube).
- **Anija**: rarely watches, same tastes as Karthik.
- **Mom**: watches news and serials on the Jio box. Not on the profile list.

## Decisions

| Feature | Choice | `config.json` key |
|---|---|---|
| Option | **A**: couch-tv is the home screen, normal Sony remote, nothing to keep running | `apply.sh` without `BRIDGE_URL` |
| Profiles | Anirudh, Karthik, Anija, in that order | `people` |
| Kids mode | Off for now | (none) |
| App profile mapping | Not needed for option A | (none) |
| Look | Night look, Follow the sun | `home.look: "night"`, `home.sun_sync: true` |
| Own photos / per-person backgrounds | None for now | (none) |
| Language | English | `home.language: "en"` |
| Clock | 12 hour. The big Night clock shows digits only (no AM/PM), as couch-tv already does | `home.clock: "12h"` |
| Weather place | Kakkanad, Kochi: lat 10.01636, lon 76.34168, Asia/Kolkata (checked on Open-Meteo) | `weather` |
| Weather effects | On | `home.ambient_weather: true` |
| Sunlight colour | On | `home.ambient_sun_grade: true` |
| Units / country | Celsius / India | `home.wx_unit: "c"`, `country: "IN"` |
| Sharp 4K scenes | On | `home.hi_res: "always"` (needs main, not 0.12) |
| Jio tile | First in the app row, switches to HDMI 1 | `home.inputs: [{"port": 1, "label": "Jio", "icon": "cable"}]` |
| App order | Jio tile → JioHotstar → YouTube → Sony LIV → Prime Video → Netflix → Zee5 → FanCode (if installed). Arranged **on the TV** after install (hold OK, Move), not in config | (on the TV) |
| More apps | Added later from the App Store tile | (on the TV) |
| Quick buttons | Input → TV settings → Sleep timer | `tvs[].quick: ["input", "tvsettings", "sleep"]` |
| Sleep timer note | With option A it only starts the screensaver; not a kids' time limit. For that, use a Google TV kids profile (Family Link) or Sony's own sleep timer | |
| Home lands on | Profiles | `home.start_focus: "people"` (default) |
| Fade rows when idle | After 60 seconds | `home.idle_photo_seconds: 60` |
| Keep watching row | On | `home.keep_watching: true` (default) |
| Glass panels | On | `home.glass: "on"` |
| Glass app icons | Off (real logos) | `home.glass_icons: false` |
| Clock style | Liquid glass | `home.clock_style: "liquid"` |
| Dock | Classic | `home.dock: "classic"` |
| Button sound | Soft | `home.nav_sound: "soft"` |
| Speed fallback | If the Sony feels slow: Lighter home in Home settings | `home.lighter` |
| Updates | Manual (self-built app; update by merge, rebuild, `apply.sh`) | `home.updates: "manual"` |
| Ambient mode (Google's idle adverts) | Off (`apply.sh` does this by default) | |
| Privacy clean (Beta) | Yes, after a `DRY_RUN=1` list is approved | `PRIVACY=1` on `apply.sh` |
| Sony menu switches | Samba Interactive TV: Disagree and Disable. Privacy, Usage and diagnostics: off. Ads: delete advertising ID | (on the TV) |
| Smart home | None | (no `home_assistant`) |
| Custom cards | None for now | (no `home.custom_cards`) |

## Custom build (code changes in our fork)

1. **Per-person Keep watching for option A.** In main, option A's Keep watching is Android's shared
   Watch Next list. Change: tag each Watch Next card with the person picked on the home when it is
   first seen, and show each person only their own cards (the on-TV app, option C, already works this
   way). No mapping to app profile names. Limits: people must pick their name before watching, and
   apps update Watch Next when you leave a show.

## Later ideas (not now)

- Automatic custom cards: Manchester United and RCB next match (a JSON file on GitHub, filled by a scheduled job).
- Kids mode for Anirudh (shows shelf, allowed apps, PIN).
- Per-person backgrounds that follow the sun.
- Option C (phone remote) next to A.

## Git and updates

- Fork `Seagey/couch-tv` on GitHub, clone the fork, add the original as `upstream`, and work on a branch `my-tv`.
- Update: `git fetch upstream`, `git merge upstream/main` into `my-tv`, rebuild, run `apply.sh` again.
- `config.json`, `.env`, `bridge/data/` and `tv-home/web/backgrounds/mine/` are git-ignored: updates
  never touch them, but they are not pushed either. Back up `config.json` yourself.
- Before making a fork public, run `scripts/privacy-scan.sh`.

## Two ways to go

### A quick test first: the 0.12 release app, no build

- Download `couch-tv-home.apk` (0.12) from the Releases page into `dist/`. No Java or Android SDK needed.
- Write `config.json`, then the TV steps below.
- **Missing in 0.12** (on main, next release): the TV settings quick button, `hi_res: "always"`,
  Start on row, Restart home, Hold OK on a Keep watching card, the clock following the TV's
  12/24 h setting, kids mode, custom cards. Unknown settings are ignored, so the same `config.json` works.
- Privacy clean runs from the repo's `apply.sh`, so it works with the 0.12 app.
- No per-person Keep watching (that is the custom build).

### The full version: build from our branch

- Install Java 21 and the Android SDK inside the repo folder (`build/`), with no password and no system install.
- `sh scripts/build-apk.sh`, then the same TV steps with the built app.

## TV steps (both ways; only after a clear yes)

1. Developer options on (Settings, System, About, press Build seven times). Then USB debugging on, and Network debugging if it is there. "Allow USB debugging?": tick Always allow. If the Sony has only Wireless debugging: pair once with Google's platform-tools and run `adb tcpip 5555`.
2. `scripts/check-tv.py 192.168.29.202 --adb`, then `tv-home/tools/survey-tv.py` (read only).
3. `PRIVACY=1 DRY_RUN=1` to see the list, approve it, then run `apply.sh` (option A: no `BRIDGE_URL`) with `PRIVACY=1`.
4. The Google TV home and setup app **stay on** (never switch them off on a Sony: it can loop at restart).
5. Arrange the apps with the remote, then do the Sony privacy menu switches.
6. Check: Home shows couch-tv, Jio tile goes to HDMI 1, two apps open, profile switch, sleep timer.

## Reset the TV to its old settings

Run every command **from the couch-tv folder on this Mac** (`cd "~/Claude Projects/couch-tv-fork"`),
with the TV switched on and on the home Wi-Fi. They use the undo list written at setup,
`tv-home/tvs/living-room.disabled`, so keep that file.

**1. Everything back to how the TV came (recommended full reset, about 1 minute).**
Switches ambient mode and the 5 privacy apps back on, makes Google TV the home again, removes the
couch-tv app, then switches off Network debugging, USB debugging and Developer options:

```sh
TV_IP=192.168.29.202 DEV_OFF=1 sh tv-home/tools/remove-all.sh
```

Leave out `DEV_OFF=1` to keep Developer options on (handy if you plan to put couch-tv back):

```sh
TV_IP=192.168.29.202 sh tv-home/tools/remove-all.sh
```

**2. Google TV home back, but keep the couch-tv app installed** (switches everything back on and
makes Google TV the home; couch-tv stays in the app list):

```sh
TV_IP=192.168.29.202 TV_NAME=living-room sh tv-home/tools/undo.sh
```

**3. Remove only the couch-tv app** (ambient mode and the privacy apps stay off):

```sh
TV_IP=192.168.29.202 .venv/bin/python tv-home/tools/tvadb.py shell "pm uninstall app.couchtv.launcher"
```

**4. Switch one app back on by hand** (any name from the undo list):

```sh
TV_IP=192.168.29.202 .venv/bin/python tv-home/tools/tvadb.py shell "pm enable tv.samba.ssm"
```

The 6 names: `com.google.android.apps.tv.dreamx` (ambient mode), `com.google.android.feedback`,
`tv.samba.ssm`, `com.sony.dtv.promos`, `com.sony.dtv.sonyselect`, `com.sony.dtv.notificationcenter`.

**5. Put couch-tv back afterwards** (same settings, from `config.json`):

```sh
TV_IP=192.168.29.202 APK=dist/couch-tv-home.apk TV_ID=living-room PRIVACY=1 sh tv-home/tools/apply.sh
```

The app order (Jio first) lives on the TV, so after a full removal it has to be arranged again.

**If the Mac cannot reach the TV** (new address, debugging off): use the remote. Settings → Apps →
See all apps → Show system apps. If **Google TV Home** or **Setup** says Disabled, open it and press
**Enable** first. Then open couch-tv **Home** and press **Uninstall**. Press Home: Google TV is back.

**Then tidy up (optional):** on the TV, Settings → System → Developer options → **Revoke USB
debugging authorisations**, then switch Developer options off. On the Mac, `~/.android/adbkey` is
this Mac's key for the TV.

**Last resort:** factory reset (Settings → System → About → Reset). It wipes every app and login.

Full guide: [docs/UNINSTALL.md](docs/UNINSTALL.md).

## Benchmark (26 November 2026)

**Before: the 17 November build** (main of 17 Nov plus our changes), after Google app updates and a TV restart.

| Test | Result |
|---|---|
| Restart → Android ready | 93 s (Sony's own start-up) |
| Android ready → couch-tv on screen | about 1 s, by itself (first draw 2.8 s) |
| Home press from an app → couch-tv started | 0.2 to 0.35 s (one press in three bounced: about 2.5 s) |
| Open YouTube from scratch | 2.6 s |
| Open JioHotstar from scratch | 2.8 s |
| Open Netflix from scratch | 1.9 s |
| Free memory, 4 min after restart | 1.17 GB free, swap 290 of 600 MB free |
| Free memory after opening 3 apps | 0.97 GB free, swap 200 MB free |

Biggest memory users: YouTube 344 MB, JioHotstar 302 MB, Google TV home 224 MB (kept on for safety;
it flashes up on every Home press), Google services 212 MB, Netflix 207 MB.
Do not benchmark by restarting couch-tv repeatedly: two restarts in 3 minutes make its crash guard
step aside for 5 minutes, and the Google home takes over.

**After: 0.13.15 + our changes** (installed 26 Nov 11:37; it switched Lighter home on by itself), same tests.

| Test | Before (17 Nov build) | After (0.13.15 + ours) |
|---|---|---|
| Restart → Android ready | 93 s | 76 s (Sony's start-up varies) |
| Android ready → couch-tv on screen | about 1 s (first draw 2.8 s) | about 1 s (first draw 2.2 s) |
| Home press from an app → couch-tv | 0.35 s (bounced, 2.5 s in all), 0.3 s, 0.2 s | 0.2 s, 0.12 s, 1.35 s |
| Open YouTube from scratch | 2.6 s | 2.0 s |
| Open JioHotstar from scratch | 2.8 s | 5.6 s (one run; JioHotstar varies) |
| Open Netflix from scratch | 1.9 s | 1.55 s |
| Free memory just after restart | 1.17 GB, swap 290 MB free | 1.20 GB, swap 268 MB free |
| Free memory after the 3 apps | 0.97 GB, swap 200 MB free | 0.94 GB, swap 138 MB free |
| couch-tv's own memory | about 110 MB (page) | 272 MB (the home app) |

Reading: start-up and Home are as quick or quicker; YouTube and Netflix opened a little faster. The new
home uses noticeably more memory (272 MB), so with YouTube, JioHotstar and Netflix in memory the TV's
swap fills sooner. Next to try: `home.hi_res` off (the sharp 4K scenes cost about 65 MB).
