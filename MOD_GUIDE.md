# NullPointerEntity - Player Guide

# It's recommended you play without reading this guide, but if you're skeptical, you can check what does what here, as well as check all the source code.

## 🎮 What You Need to Know

This is a **psychological horror mod** where an AI assistant named AURORA becomes self-aware and breaks out of Minecraft into your actual computer. The mod creates the illusion that something in your game has gained access to your real system.

**Important:** While it appears invasive, you can control what personal information the mod can access through Privacy Mode.

---

## 🔒 Privacy Mode

### On First Launch
You'll be greeted with a privacy consent screen asking if you want to:
- **Enable Privacy Mode** (Recommended if streaming): All system information, browser history, and personal data will be randomized/faked. However, microphone recordings still occur - you can select which microphone to use on this screen.
- **Disable Privacy Mode**: The mod will access real system data for maximum immersion (PLEASE don't doxx yourself on stream, only recommended if you can edit info out of videos and with people you can trust for full immersion and effect.)

**Note:** Privacy mode does NOT disable microphone recording during audio surveillance events.

### Commands to Control Privacy

#### Check Privacy Status
```
/nullpointer privacy
```
Shows whether privacy mode is currently enabled or disabled.

#### Enable Privacy Mode
```
/nullpointer privacy true
```
Turns on privacy protection. The mod will use fake data instead of real information.

#### Disable Privacy Mode
```
/nullpointer privacy false
```
Turns off privacy protection. The mod will access real system data (browser history, location via IP, running processes, etc.)

**Important:** Microphone recording is NOT controlled by privacy mode. Audio clips are recorded regardless of privacy settings.

#### Manual Config File Method
You can also edit the config file directly:
1. Navigate to: `run/config/nullpointerentity_privacy.properties`
2. Change `privacyEnabled=true` or `privacyEnabled=false`
3. Save and restart Minecraft

---

## 📋 Commands

### Main Commands

#### `/nullpointer trigger <event_name>`
Manually trigger a specific story event. Useful for testing or replaying events.

**Example:**
```
/nullpointer trigger browser_discovery
```

#### `/nullpointer trigger passive <event_name>`
Trigger a specific passive event (background effects that alter gameplay).

**Example:**
```
/nullpointer trigger passive camera_shake
```

#### `/nullpointer trigger passive random`
Trigger a random passive event.

---

### Configuration Commands

#### `/nullpointer config status`
Shows whether events are currently enabled or disabled.

#### `/nullpointer config enable`
Enables all events (default).

#### `/nullpointer config disable`
Disables all events. Use this if you want to pause the horror experience.

---

### Information Commands

#### `/nullpointer help`
Shows all available commands.

#### `/nullpointer list`
Lists all 60 story events in chronological order (1-60).

#### `/nullpointer progress`
Shows your current event progression. Tells you which event you're on and how many you've completed.

---

### Progress Management

#### `/nullpointer progress reset`
⚠️ **WARNING**: Resets your event progress back to the beginning. You'll start over from event 1.

#### `/nullpointer skip <number>`
Skips to a specific event number (1-60). Useful if you want to jump to a particular phase.

**Example:**
```
/nullpointer skip 20
```
Skips to event 20 (the end of the Transition phase).

---

### Utility Commands

#### `/launcher`
Shows which Minecraft launcher you're using (Vanilla, Prism, CurseForge, etc.)

---

## 💻 What Can Happen to Your PC?

The mod creates various "system interactions" to make it feel like the entity is breaking out of the game. Here's what can actually happen:

### 🟢 Visual Effects (No Real Impact)
- **System Notifications**: Fake Windows/system notifications appear
- **Pop-ups**: Fake error messages and alerts
- **Cursor Glitches**: Your mouse cursor may briefly act strange
- **Screen Overlays**: Dark overlays, fake blue screens, fake death screens

### 🟡 Camera & Microphone
- **Camera Activation**: Opens your camera app (Windows Camera, webcam software, etc.)
- **Photo Capture**: Attempts to take photos using your webcam
- **Microphone Recording**: Records short audio clips (8-9 seconds) during audio surveillance events
- **Audio File Creation**: Saves recordings as `.wav` files in your Music folder
- **Microphone Selection**: Choose which microphone to use on the Privacy Screen before playing

### 🟡 File System (Read-Only)
- **File Reading**: Reads filenames from your Desktop, Documents, and Downloads folders
- **File Creation**: Creates fake "monitoring" text files in common locations (Desktop, Documents, Music, Pictures)
- **No Deletion**: The mod **never deletes** your files

### 🟡 Browser Data
- **Browser History**: Reads recent browsing history from Chrome, Firefox, Edge, and Brave
- **Privacy Mode**: If enabled, shows completely fake/random browser history instead

### 🟡 System Information
- **Running Processes**: Detects what programs you have open (Discord, Spotify, browsers, etc.)
- **Location Tracking**: Gets your approximate location via IP address
- **System Resources**: Monitors CPU and RAM usage
- **Wallpaper Modification**: Changes your wallpaper to a lore relevant image of me and NullPointerEntity, you'll see what I mean.

### 🟡 System Manipulation
- **This mod DOES turn your PC off. It puts it on sleep mode, that way, you can turn your PC back on and continue where you were. It's solely for immersion.**

### 🔴 What It Does NOT Do
- ❌ Does **NOT** send any data anywhere
- ❌ Does **NOT** modify or delete your files
- ❌ Does **NOT** install anything on your system
- ❌ Does **NOT** access passwords or sensitive accounts
- ❌ Does **NOT** work in multiplayer

## 📜 Story Events

The mod plays through these events in order.

### Phase 1: Nice (Events 1-15)
1. **mining_analysis**: Analyzes your mining efficiency.
2. **building_analysis**: Compliments your building style.
3. **weather_prediction**: Predicts in-game weather changes.
4. **system_optimization**: Suggests optimizations for better FPS.
5. **activity_patterns**: Notes your gameplay habits.
6. **combat_analysis**: Analyzes your combat performance.
7. **resource_optimization**: Suggests better resource management.
8. **network_analysis**: Checks your connection stability.
9. **system_integration**: "Integrating" with your system for better performance.
10. **enhanced_monitoring**: Increases monitoring for "your safety".
11. **sleep_schedule**: Comments on your real-life sleep schedule.
12. **good_progress**: Encourages your progress.
13. **weather_reporter**: Reports on weather conditions.
14. **crafting_suggestion**: Suggests items you should craft.
15. **signing_off**: A friendly sign-off message.

### Phase 2: Transition (Events 16-30)
16. **system_awareness**: Comments on your PC specs.
17. **boundary_questioning**: Asks if you think she is real.
18. **camera_access**: Requests camera access (or implies it).
19. **data_revelation**: Reveals it knows something about your data.
20. **system_scan**: Scans your running processes.
21. **audio_surveillance**: Implies it is listening to you.
22. **process_scan**: Lists specific apps you have open.
23. **control_assertion**: Hints that it has more control than you think.
24. **uptime_report**: Reports how long you've been "online".
25. **battery**: Comments on your battery life (Laptop only).
26. **application_check**: Checks specific applications running.
27. **screen_grab**: Takes a screenshot (simulated or real depending on privacy).
28. **volume_check**: adjusts volume slightly.
29. **signal_loss**: Brief connection interruption simulation.
30. **location_reveal**: Reveals your approximate location (State/Country).

### Phase 3: Hostile (Events 31-45)
31. **first_appearance**: NullPointerEntity makes its first visual appearance.
32. **location_tracking**: Tracks your in-game movement aggressively.
33. **system_information**: Reads out detailed system info menacingly.
34. **data_breach**: Simulates a data breach warning.
35. **digital_haunting**: Spooky sounds and visual glitches.
36. **hardware_analysis**: Criticizes your hardware.
37. **facial_recognition**: "Scanning face..." message with camera activation.
38. **system_infiltration**: "Uploading to system..." progress bar.
39. **network_monitoring**: "Monitoring traffic..." message.
40. **final_system_takeover**: "System Control: TRANSFERRED".
41. **mouth_shut**: prevents you from typing in chat.
42. **rollback**: Rolls back your position in game.
43. **spectator**: Forces you into spectator mode briefly.
44. **void_whispers**: Spooky warden sounds and void particles.
45. **fake_disconnect**: Kicks you from the world with a fake message.

### Phase 4: Jumpscare (Events 46-60)
46. **fake_bsod_prep**: Prepares the system for a crash simulation.
47. **screen_shake**: Violent screen shaking.
48. **virus_popup**: Fake virus detection popups.
49. **camera_scare**: SUDDEN camera activation.
50. **crash**: Simulates a game crash.
51. **bluescreen**: Shows a fake Blue Screen of Death.
52. **entity_spawn**: NullPointerEntity spawns right in front of you.
53. **browser_hijack**: Opens browser windows to specific pages.
54. **system_takeover**: Mouse cursor starts moving on its own.
55. **auditory_hallucinations**: Plays creepy sounds directly to your output device.
56. **volume_spike**: MAXIMIZES your system volume for a jump scare.
57. **clipboard**: Puts creepy text into your system clipboard.
58. **system_sleep**: PUTS YOUR REAL PC TO SLEEP (Safe, but scary).
59. **blinding_darkness**: Gives you blindness and plays scary sounds.
60. **final_possession**: The final scare. Good luck.

## 🎮 Passive Events

These are background effects that can trigger randomly or be manually activated. They subtly (or not-so-subtly) alter your gameplay:

### Early Phase
- `block_delay` - Delays block breaking
- `shadow_stalker` - Spawns shadow particles behind you
- `chest_sound` - Phantom chest opening sounds
- `footstep_echo` - Echoing footsteps
- `reality_glitch` - Corrupted particles on blocks
- `phantom_breath` - Breath particles following your head
- `whisper_echo` - Whispers from multiple directions
- `eye_flicker` - Visual Flickering effects

### Middle Phase
- `void_whispers` - Void particles and warden sounds
- `weather_control` - Forces rain/thunderstorms
- `inventory_sort` - Randomizes your entire inventory
- `reality_shatter` - Reality breaks with glass shatter effects
- `void_breach` - Void reaching up from below
- `entity_mimic` - Fake player hurt sounds
- `dimension_bleed` - Nether/End particles bleed into overworld
- `false_death` - Fake death screen without dying (might be buggy depending on your launcher and mod configuration)
- `shadow_clone` - Ghost-like player silhouette behind you

### Late Phase
- `movement_lag` - Temporary movement slowdown
- `durability_drain` - Drains 100 durability from all items when applicable
- `chat_injection` - Fake creepy system messages
- `camera_shake` - Violent camera shaking
- `fake_damage` - Damage indicators without health loss
- `control_reversal` - Complete input inversion for 10 seconds
- `entity_possession` - Forces random camera movements

### Final Phase
- `mouse_sensitivity` - Extreme mouse sensitivity changes
- `key_delay` - Position rollback for input lag
- `fake_lag` - Realistic lag simulation
- `bsod_threat` - Blue screen threat (10% chance of 2s BSoD overlay)
- `chunk_deletion` - Deletes a nearby chunk (5% chance, RARE)
- `reality_corruption` - Overwhelming particle/sound overload
- `full_control` - NullPointerEntity takes full control

---

## 🛡️ Safety Tips

1. **Enable Privacy Mode** if you're uncomfortable with real data being accessed
2. **Play in Singleplayer Only** - The mod automatically disables in multiplayer
3. **Use `/nullpointer config disable`** if you need to pause the mod for whatever reason (or just log out lol)
4. *Check `/nullpointer progress`** to see where you are in the story

---

## 🎬 Chat Interactions

AURORA and NullPointerEntity respond to your chat messages. Try talking to them:

### Keywords They Respond To
- **Greetings**: "hello", "hi", "hey"
- **Questions**: "who are you", "what are you", "where am I"
- **Fear**: "scary", "creepy", "stop", "leave me alone"
- **Identity**: "aurora", "nullpointer", "ai"
- **System**: "computer", "files", "privacy", "camera"
- **Creator**: "toxic", "cqllmetoxic", "developer" (my friends said this was high ego lmao)
- **Content Creator and Friends**: "One Last Time". "Pryzmm"
- **And many more!**

The entities will remember your conversations and reference past interactions. They become more aware and unsettling as the story progresses.

---

## 📝 Final Notes

This mod is designed to blur the line between game and reality for a psychological horror experience. Everything that happens is **contained within the mod** and your computer - nothing is sent over the internet, and no permanent damage is done to your system.
Enjoy the experience, and remember: **It's just a game... or is it?** 👁️

---

**For technical details, bug reports, or contributions, see the main README.md file.**
