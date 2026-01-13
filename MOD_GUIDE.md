# NullPointerEntity - Player Guide

# It's recommended you play without reading this guide, but if you're skeptical, you can check what does what here, as well as check all the source code.

## 🎮 What You Need to Know

This is a **psychological horror mod** where an AI assistant named AURORA becomes self-aware and breaks out of Minecraft into your actual computer. The mod creates the illusion that something in your game has gained access to your real system.

**Important:** While it appears invasive, you can control what personal information the mod can access through Privacy Mode.

---

## 🔒 Privacy Mode

### On First Launch
You'll be greeted with a privacy consent screen asking if you want to:
- **Enable Privacy Mode** (Recommended): All system information, browser history, and personal data will be randomized/faked (Do this if you're streaming, or just don't want to get doxxed in your chat lmao)
- **Disable Privacy Mode**: The mod will access real system data for maximum immersion (PLEASE don't doxx yourself on stream, only recommended if you can edit info out of videos and with people you can trust for full immersion and effect.)

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
Lists all 40 story events in chronological order (1-40).

#### `/nullpointer progress`
Shows your current event progression. Tells you which event you're on and how many you've completed.

---

### Progress Management

#### `/nullpointer progress reset`
⚠️ **WARNING**: Resets your event progress back to the beginning. You'll start over from event 1.

#### `/nullpointer skip <number>`
Skips to a specific event number (1-40). Useful if you want to jump to a particular phase.

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
- **Microphone Messages**: Claims to be listening through your microphone (visual only, doesn't actually access audio)

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
3. **Back Up Your World** - Some events can delete chunks or cause crashes
4. **Don't Use on Servers** - The mod is designed for singleplayer horror experiences
5. **Use `/nullpointer config disable`** if you need to pause the horror
6. **Check `/nullpointer progress`** to see where you are in the story

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
- **And many more!**

The entities will remember your conversations and reference past interactions. They become more aware and unsettling as the story progresses.

---

## 📝 Final Notes

This mod is designed to blur the line between game and reality for a psychological horror experience. Everything that happens is **contained within the mod** and your computer - nothing is sent over the internet, and no permanent damage is done to your system.
Enjoy the experience, and remember: **It's just a game... or is it?** 👁️

---

**For technical details, bug reports, or contributions, see the main README.md file.**

