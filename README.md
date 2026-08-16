# PitHelper (Fabric 26.2)

Fabric 26.2 port of [ChrisTechs/PitHelper](https://github.com/ChrisTechs/PitHelper). The original is **Forge 1.8.9** (`v2` on that repo); this tree is Fabric-only and keeps the same event HUD, config menu, commands, and BrookeAFK feed.

Fabric version branches (not Stonecutter): `1.21.11` and `26.2` (this tree).

## What works in this port

- Event HUD (next events + active event) from BrookeAFK
- `/pithelper` config / live events / API / social / dev tools
- `/pitfriend`, `/pitenemy`, `/apiexplorer`, `/viewinv`
- Chat and visual event notifications, plus on-screen countdown titles
- Quick Maths solver (clipboard / open chat — does not auto-send)
- Auto-spawn keybind and death movement block
- Lobby prestige scanner, friend/enemy join alerts, and chat name colors
- Friend/enemy/iron/chain player tints
- PitHelper.org API explorer and profile/inventory viewer

Hypixel location uses `/locraw` (hidden from chat) plus scoreboard titles. The original Forge build used Hypixel Mod API packets instead.

## Build

Requires JDK 25 (Minecraft 26.2). Fabric Loader 0.19.3 + Fabric API for 26.2.

```bat
gradlew.bat build
```

Copy `build\libs\pithelper-2.0.0-fabric.jar` into `.minecraft\mods`.

## License

GNU Affero General Public License v3.0. Copyright (c) 2026 Christian Steenkamp. This Fabric port is a derivative work.
