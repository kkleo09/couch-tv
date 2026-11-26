# My TV: decisions, pending items and security notes

The decision log for the `my-tv` branch of this fork (Sony BRAVIA BF1 Google TV, Android 12, option A).
Setup steps, benchmarks and reset commands are in [MY-TV-PLAN.md](MY-TV-PLAN.md). Add a line here
whenever something is decided, done or left for later.

## Decisions

| Date | Decision | Why | Alternatives looked at |
|---|---|---|---|
| 17 Nov | **Option A** (couch-tv as the home, no computer left on) | No computer stays on at home; the normal Sony remote must keep working | B (needs an always-on computer), C (phone remote; re-pairing after restarts is too fiddly for the household) |
| 17 Nov | **Jio tile first** in the app row (HDMI 1) | Mom watches news and serials on the Jio set-top box | Input quick button alone |
| 17 Nov | Profiles **Anirudh, Karthik, Anija**; no profile for Mom | Mom uses the Jio box, not the apps | |
| 17 Nov | **Kids mode parked** | Not needed yet; Google TV kids profile / Family Link is the better tool for screen-time limits | couch-tv kids mode with a PIN |
| 17 Nov | **Per-person backgrounds dropped**; everyone shares Follow the sun | Keep it simple | Own scenes per person (custom build) |
| 17 Nov | **Custom cards later** (Man United / RCB match cards) | Needs a data source and a scheduled job | |
| 17 Nov | **Updates: manual** | The app is self-built, so it cannot take the official update; updates come by merging upstream and rebuilding | notify |
| 17 Nov | **Privacy clean on**: ambient mode, Samba TV ACR, Sony promos / Select / notification centre, Google feedback switched off | Samba TV reads what is on screen, HDMI included; the rest is advertising | Leave them on |
| 17 Nov | **Custom 1: three names side by side** (round icon only from four people) | One press to switch person | Colours per person on the round icon |
| 17 Nov | **Custom 2: per-person Keep watching** for option A, seeded by `home.keep_watching_owners`, "Move to" on hold OK | Option A's Keep watching was one list for the house; app profile names cannot be read | Option B or C (computer or ADB) |
| 17 Nov | **Custom 3: sleep timer that pauses and switches the TV off** (Home button watch, lock screen action) | The stock timer did nothing while an app played | Device admin "lock now" (harder undo) |
| 26 Nov | **Fork `kkleo09/couch-tv`, branch `my-tv`, on upstream 0.13.15** | Keep our changes and merge new releases | Plain copy (no merges) |
| 26 Nov | **Push `config.json` and these notes** (names and suburb are fine to publish) | One place for the setup | Keep them local only |
| 26 Nov | **Sharp 4K scenes off** (`hi_res: false`) | No measurable gain on a TV that draws at 1080; memory is the TV's limit | Keep 4K |
| 26 Nov | **Lighter home on** (0.13.15 switched it on by itself) | The TV has 2.4 GB of memory | |
| 26 Nov | **Custom 4: Memory button + low-memory question; restart via the power menu** | This Sony refuses `killBackgroundProcesses` for other apps ("Invalid packageName"); a restart frees the most | Close apps over the TV's own ADB (later, see pending); Developer options background process limit (blunt) |
| 26 Nov | **Custom 5: couch-tv on top after a restart** using usage access | The Google home sometimes stayed after a restart (watch bound too late) | Extend couch-tv's TCL/Fire TV rule to all TVs (would cover an HDMI input at power on) |

## Test results worth keeping

- 26 Nov: after a restart the Google home flashes for about 4 s (black screen with a Continue watching row),
  then couch-tv comes on top. Accepted: not much delay.
- 26 Nov: restarting while on the **Jio input works**: couch-tv does not cover the Jio picture.
- 26 Nov: Restart in the power menu works on the first press (two checks).
- Benchmarks before and after 0.13.15: see MY-TV-PLAN.md.

## Pending and ideas

| Item | State |
|---|---|
| Sony privacy menu switches (Samba Interactive TV: Disagree and Disable; Usage and diagnostics off; delete advertising ID) | To do on the TV, or confirm done |
| FanCode | Install from the App Store tile, then move it to the end of the row (Home settings, Apps) |
| The ~4 s Google home flash after a restart | Accepted for now; could be shortened by checking sooner after boot |
| Close idle apps for real | Would need couch-tv to use the TV's own ADB (as the option C app does): one "Allow USB debugging?" for couch-tv |
| Translations of the my-tv texts ("Move to", Memory) | English only; fine while the home is in English |
| Man United / RCB match cards | Later project |
| Kids mode for Anirudh | Parked |
| Offer the custom changes upstream (three names, per-person Keep watching, sleep timer, memory, boot check) | Not done; would save merging them by hand on every update |
| Updating to new couch-tv releases | `git fetch upstream`, `git merge upstream/main` into `my-tv`, rebuild, `apply.sh` with `SCREEN_TIME=1`, then `appops write-settings` |

## Security and privacy notes

**On the TV**
- **Developer options and USB debugging are on, and ADB listens on port 5555** on the home Wi-Fi. Only
  computers the TV said "Always allow" to can connect (this Mac's key, `~/.android/adbkey`). Anyone with
  that key on the Wi-Fi has full control of the TV. Keep the Mac's `~/.android` private; on the TV,
  Developer options, **Revoke USB debugging authorisations** forgets every computer.
- couch-tv's **Home button watch** is an accessibility service. It only sees which app came to the front
  (window events), never the screen's contents. It also performs the lock screen action (sleep timer) and
  opens the power menu (Memory button).
- **Usage access** (`GET_USAGE_STATS`, granted by `apply.sh SCREEN_TIME=1`): couch-tv reads Android's
  record of which app came to the front, only in the first 2 minutes after a restart. Nothing leaves the TV.
- Also granted by `apply.sh`: write secure settings (to keep the Home button watch on), modify system
  settings (screensaver button), notification access for the Spotify card, TV listings.
- Apps switched off (not removed), undo list `tv-home/tvs/living-room.disabled`: `dreamx`,
  `com.google.android.feedback`, `tv.samba.ssm`, `com.sony.dtv.promos`, `com.sony.dtv.sonyselect`,
  `com.sony.dtv.notificationcenter`. Full undo: MY-TV-PLAN.md, "Reset the TV to its old settings".
- Sony's Restart is logged as an unclean shutdown: settings changed in the last minutes can be lost.
  After changing permissions, run `appops write-settings` over ADB.

**In this repository (public)**
- Published on purpose: first names, the suburb (Kakkanad) with its coordinates, the TV's home-network
  address (192.168.29.202, only reachable inside the home Wi-Fi), the household's shows.
- Kept out: the TV's MAC address, email addresses (commits use the GitHub noreply address
  `…+kkleo09@users.noreply.github.com`), API keys or tokens (there are none), photos.
- Run `scripts/privacy-scan.sh --no-history` before every push. Expected findings: the 192.168 address
  and `config.json` (both approved).

**On the Mac**
- **Signing key** `~/.android/debug.keystore`: every build is signed with it, and only a build with the
  same key installs over the TV's app and keeps its settings. Back it up; never commit it.
- **ADB key** `~/.android/adbkey`: the TV trusts it (see above). Never commit it.
- **GitHub login**: `gh` stores it in the macOS keychain. `gh auth logout` removes it.
- Build tools (JDK 21, Android SDK) are inside `~/Claude Projects/Couch-TV/build`, not installed system-wide.
