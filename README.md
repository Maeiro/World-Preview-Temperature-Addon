# World Preview Temperature Addon

A Forge (`1.20.1`) addon that adds a **biome base temperature** map view to **World Preview**.

This project is an unofficial addon and **directly depends** on `world_preview`.

## Features

- New biome temperature map mode in the preview.
- Tooltip with `BT` (biome base temperature) for the hovered biome.
- Optional ocean override (render ocean biomes as light gray).
- Biome list colors synchronized with temperature mode.
- Controls integrated into the World Preview settings screen.

## Requirements

- Minecraft `1.20.1`
- Forge `47.x`
- World Preview `1.3.1+` (required)
- Java `17`

## Installation

1. Install a Forge 1.20.1-compatible `world_preview`.
2. Place this addon jar into your instance `mods` folder.
3. Launch the game and open World Preview.

## Configuration

Addon options are available inside World Preview settings (`General` tab):

- `Enable biome temperature view`
- `Biome temperature: oceans as gray`

Note: both toggles default to `true` for new configs/missing fields.

## Build (Windows / PowerShell)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean build
```

Output jar:

`build\libs\world-preview-temp-addon-1.0.0+forge-1.20.1.jar`

## Local Development

During build, the project tries to resolve `world_preview` classes in this order:

1. `../world-preview/build/classes/java/main`
2. `../world-preview-1.20.1/build/classes/java/main`
3. `./libs/world_preview-1.3.1.jar`

If you use option 3, jars in `libs/` are git-ignored (`libs/*.jar`).

## Compatibility

- Loader: Forge
- Target Minecraft version: `1.20.1`

## License

This addon is licensed under **MIT**. See `LICENSE`.

`world_preview` is a separate project licensed under **Apache-2.0**.
