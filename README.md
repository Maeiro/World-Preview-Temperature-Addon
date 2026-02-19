# World Preview Temperature Addon

Forge addon for Minecraft 1.20.1 that extends World Preview (`world_preview`).

## Requirements

- Minecraft `1.20.1`
- Forge `47.x`
- World Preview `1.3.1+` (required dependency)
- Java 17

## Build

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
.\gradlew.bat build
```

Output jar:

`build\libs\world_preview_temperature_addon-<version>.jar`

## Status

Project scaffold is ready and wired as a strict addon dependency through `mods.toml`.
Feature mixins for temperature UI/render integration are the next step.
