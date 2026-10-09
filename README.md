# Mana Inspector

A lightweight **Botania addon** for **Minecraft 1.21.1 + NeoForge**.

When your crosshair points at a Botania mana pool, a client-side HUD displays:

- Current mana and maximum capacity
- Stored mana percentage
- A live progress bar

No interaction or GUI opening is required. The HUD is hidden when the crosshair is not on a Botania mana pool.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Botania for Minecraft 1.21.1
- Java 21

## Build

Install JDK 21 and Gradle 8.10 or newer, then run:

```bash
gradle build
```

The mod JAR is written to `build/libs/`.

## Notes

Mana Inspector reads the pool block entity's public `getCurrentMana()` and `getMaxMana()` methods at runtime, avoiding a compile-time dependency on Botania internals. Botania must still be installed in the game.

This repository's initial implementation has not yet been verified in a live Minecraft client. Please report compatibility issues with the exact Botania and NeoForge versions.

## License

MIT. Botania remains a separate dependency and is distributed under its own license.
