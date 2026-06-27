# Multiplayer

## In one line

Multiplayer works just like single player. **Privacy Mode is the only thing that decides how
invasive the mod gets** — multiplayer adds no extra restrictions. The two real differences are
that the story is **shared** by everyone in the world, and the system-touching effects run on
**each player's own machine** rather than being collected on one computer.

## Privacy Mode is the switch

Everything invasive is controlled by one setting, in single player and multiplayer alike:

- **Privacy Mode ON (the default)** — the mod still performs all of its system effects (writes
  files, runs PowerShell, opens the webcam/microphone, takes screenshots, reads the clipboard,
  sleeps the PC, spikes the volume, changes the wallpaper), but every piece of *information* it
  shows or writes is **randomized**: a fake username, fake IP, fake location, fake browser history,
  fake CPU/memory figures. The scares all fire — they just never reveal your real identity or
  location data.
- **Privacy Mode OFF** — the same effects, but with your **real** information: real username, real
  IP/location, real browser history, real system specs.

Privacy Mode is purely a *data-anonymization* switch — it changes what information is shown, not
whether the effects run. Multiplayer uses the same switch **per player**: your Privacy Mode governs
the information shown on your own machine.

## How multiplayer differs from single player

There are only two structural differences:

1. **One shared story for the whole world.** Instead of each player having their own copy of the
   60-event AURORA → NullPointerEntity story, there is a single storyline for the save. Events
   fire for **everyone online at the same time** (same chat lines, same entity appearances, same
   jumpscares), and progress advances on a shared timer as long as someone is online. Players who
   join partway through are **caught up instantly** — snapped to the world's current point and
   shown a short "story sync" message (e.g. `event 27/60 [TRANSITION]`) instead of replaying the
   earlier events.

2. **The system-touching effects run on each player's own machine.** When an event has a
   per-machine part (reading your data, dropping a file, opening your webcam, reading your
   clipboard, sleeping your PC), the server doesn't run it centrally — it asks **each player's own
   client** to run that part locally, against **that player's** computer, governed by **that
   player's** Privacy Mode. The data never travels back to the server, and one player's machine is
   never read on another player's behalf. So in a LAN game with Privacy Mode off, every participant
   sees **their own** IP / files / webcam — not the host's.

   The world-deletion finale acts on the shared save, so it stays server-driven — but the forced
   game crash that accompanies it (and every other "crash your game" beat) is sent to each player's
   **own** client, so a dedicated server is never terminated for everyone.

## How to play it

A host playing alone counts as single player. The session becomes "multiplayer" the moment a
**second player connects**, or whenever the mod runs on a **dedicated server** — but either way,
Privacy Mode still governs everything.

**Open to LAN (easiest):**

1. Start or load a single-player world that has the mod installed.
2. Pause → **Open to LAN** → **Start LAN World**.
3. Others on the same network join via **Multiplayer** (the LAN entry), or **Direct Connect** to
   your `IP:port`.

**Dedicated server:**

1. Put the mod (plus Fabric Loader + Fabric API) in the server's `mods/` folder.
2. Players connect normally. The chat narrative and per-player client effects run on the **players'**
   machines; the server itself has no display or webcam, so it never performs OS-level actions.

**Good to know:**

- **Install the mod on clients too.** The world/host drives the story, but the per-machine effects
  and client-side visuals only run for players who also have the mod. A vanilla client can still
  join and see the chat narrative — it just won't get the visual effects or per-machine parts.
- **Each player's own Privacy Mode is used**, and it updates live — toggling it re-sends your
  consent to the server immediately, no reconnect needed.
- **Privacy Mode OFF exposes your own machine, not anyone else's.** Because the per-machine effects
  run client-side, turning Privacy Mode off only affects the computer you're sitting at. The shared
  *chat narrative* can still mention the host's identity in a few not-yet-split story beats (see
  "Current state" below), but invasive actions — files, webcam, clipboard, sleep — happen on each
  player's own machine.
- **Commands act on the shared story.** `/nullpointer skip`, `/nullpointer trigger`, and the
  passive-event commands move the **world's** shared progression in multiplayer, so they advance
  the story for everyone at once.
- **Avoid commas in the world folder name** — as in single player, a comma in the world name
  breaks the per-world save file.

## Quick reference

| | Privacy Mode ON | Privacy Mode OFF |
|---|---|---|
| **Single player** | all effects fire (files, webcam, mic, clipboard, sleep, control effects) but with **randomized info** | same effects, with **real** info |
| **LAN host** | same as single player ON — on the **host's own** machine | same, with real info |
| **Joining LAN player** | shared narrative + all per-machine effects on **their own** machine, with randomized info | shared narrative + effects with **their own real** info |
| **Dedicated server** | players' clients run their own per-machine effects (randomized info); the headless server still performs no OS-level actions | players' own machines run the effects with real info; the server performs no OS-level actions |

---

## Under the hood

**Privacy Mode — `privacy/PrivacyManager`.** `isPrivacyEnabled()` returns the effective toggle
(default ON). Privacy Mode is a **data-anonymization** switch, not an action switch: it never blocks
an effect — files still write, PowerShell/webcam/mic/clipboard/sleep still run. What it changes is
the *information*. When ON, the username (via `NullPointerEntity.getDisplayUsername()` /
`ClientEventExecutor.winUser()` → `PrivacyManager.getSystemUsername`), IP/location
(`monitoring/LocationTracker`), browser history (`monitoring/BrowserHistoryReader`), and the
`system/` simulators all return randomized values; when OFF they return real values. In multiplayer
each client also receives the host's session setting (`SessionPrivacyPayload`) as a session
override, so the whole session follows a consistent Privacy Mode. Toggling fires a change-listener
so the client re-sends its consent to the server right away.

**Per-client event execution — `network` + `client/event/ClientEventExecutor`.** Story events are
split: the **server handler** keeps the shared in-game horror (sounds, entities, status effects,
fake screens, world changes), and delegates the **per-machine part** to the player's own client
with `ServerNetworking.sendRunEvent(player, eventId)`. The client's `ClientEventExecutor.run(id)`
reads *its own* machine and shows *its own* messages/files. The data it shows/writes is anonymized
under Privacy Mode (faked username/IP/history), but the effect itself still runs. Nothing is sent
back to the server. This covers the transition + hostile phases (events 16–45) and most of the
jumpscare phase (clipboard, camera, system sleep, volume spike, crash report, BSOD/takeover/virus
logs, etc.).

**The sink gate — `aurora/SystemInteractionHandler`.**
- `canRunSensitiveSystemActions()` returns `true` unless the code is running on a headless
  dedicated server (`EnvType.SERVER`). This is the **only** hard block on file creation.
- `createSystemFileInCommonLocation(...)` writes the file regardless of Privacy Mode (Privacy Mode no
  longer blocks it). Anonymization happens in the *content*: callers build file text from the display
  username + source-faked IP/location/history, so under Privacy Mode the file holds randomized info.
- `monitoring/BrowserHistoryReader.getRecentHistoryAsync(...)` returns **fake** history when Privacy
  Mode is on.

**Detecting the environment — `util/MultiplayerDetection`.** `isMultiplayerServer(server)` is
`true` for a dedicated server, a non-single-player server, or any session with more than one
player. It is used by the **story engine** to pick shared vs. per-player progression — not to
restrict invasive features.

**Networking — `network/`.** Custom payloads (IDs in `PacketIds`):

| Payload | Direction | Purpose |
|---|---|---|
| `SessionPolicyPayload` | S→C | "server has the mod" handshake probe (its policy value is informational and no longer gates anything) |
| `SessionPrivacyPayload` | S→C | the host's Privacy Mode for the session; each client applies it as a session override |
| `RunEventPayload` | S→C | asks the receiving client to run a story event's per-machine part locally, against its own machine |
| `ClientCapabilitiesPayload` | C→S | client can render effects |
| `PrivacyConsentPayload` | C→S | the client's consent (= Privacy Mode OFF), sent on join and on every toggle |
| `EffectTriggerPayload` | S→C | tells the client to play a client-only effect (fake death, or crashing the player's own game) |

`ConsentState` holds each player's consent server-side (per-UUID, cleared on disconnect).

**Shared story engine — `events/trigger/EventTriggerSystem`.** Single player uses a per-player
timer and per-player progress (`PersistentPlayerData.totalEventsExperienced`). Multiplayer uses a
world-level counter (`PersistentWorldData.sharedEventsExperienced`) and one shared timer; the tick
loop keeps every online player clamped to the shared value and fires each event for everyone at
once. Late joiners get caught up. Events dispatch by phase: 1–15 `AuroraEvents`, 16–30
`TransitionEvents`, 31–45 `HostileEvents`, 46–60 `JumpscareEvents`.

**Persistence — `data/PersistentDataManager`.** Per-world JSON (`nullpointer_entity_data.json`);
the shared counter lives in `PersistentWorldData`. Privacy / first-run state is separate and global
(`config/nullpointerentity_privacy.properties`).

The mod never uploads anything and never deletes or modifies existing user files.

## Current state (per-machine migration)

Every per-machine *file* / *effect* part now runs on each player's **own** client:

- **Phase 1 helpful events 1–2** (`AuroraEvents`) — the mining/building analysis files are built
  from server-authoritative Minecraft statistics and shipped to each client with `WriteFilePayload`,
  which writes them locally (privacy-gated in the sink).
- **Events 16–45** and the **jumpscare phase** — run via `RunEventPayload` → `ClientEventExecutor`.
- **`browser_hijack` (53)** and **`final_possession` (60)** — their desktop/document log files are
  now written by `ClientEventExecutor.runJ53` / `runJ60`, built from **each client's own** username
  and browser history. Only the world-deletion itself stays server-side (it acts on the shared
  save). The 53/60 *chat narrative* is still server-side; under Privacy Mode it shows faked data, so
  it never reveals one player's real data to another.

**Crashes never kill a dedicated server.** The forced game crashes (events 50 and 60, plus the
entity stare-down crash) are sent to the targeted player's own client via `EffectTriggerPayload`
(`crash_game`); the server itself never calls `System.exit`. The one exception is the world-deletion
finale on an **integrated** server (singleplayer / LAN host), which still force-exits *its own* JVM
so the shutdown hook can release file locks and delete the world — guarded by `!server.isDedicated()`
so it can never happen on a dedicated server.
