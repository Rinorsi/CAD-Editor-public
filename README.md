[English](./README.md) | [简体中文](./README_CN.md)

## Where to download CAD Editor

[![CurseForge](https://cf.way2muchnoise.eu/1352735.svg?badge_style=for_the_badge)](https://legacy.curseforge.com/minecraft/mc-mods/cad-editor)
[![Modrinth](https://img.shields.io/modrinth/dt/cad-editor?label=Modrinth\&style=for-the-badge\&logo=modrinth)](https://modrinth.com/project/cad-editor)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](https://mit-license.org/)

---

## What is CAD Editor?

A simple, in-game visual editor that lets you edit items, blocks, and entities right inside your current world.
It supports Minecraft’s **data components** system (1.20.5+), covering common gameplay fields like Custom Model Data, attributes, enchantments, name/lore, container & loot tables, effects, sign text, and more.

CAD Editor (Component And Data Editor) is a refactored and actively maintained continuation based on IBE Editor (original by Skye, MIT license). I am not affiliated with Skye and this is not an official version; all original credits and notices are preserved.

Original project address:
https://github.com/skyecodes/IBE-Editor

## Key features

* **In-game GUI editing** – what you see is what you get, then grab the result instantly
* **Component-first workflow** – designed for 1.21+ `components`, with common NBT use cases still considered
* **Wide coverage** – frequently used fields and advanced gameplay bits for items/blocks/entities
* **Quick copy/export** – one-click copy to command for easy sharing and debugging

## Installation & requirements

* **Minecraft:** currently 1.21.10 only (more versions planned)
* **Loaders:** NeoForge or Fabric
* **Fabric users:** also install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
* **Java:** 21 recommended

## Release notes

* 1.21.8 migration: legacy `HideFlags`, `!minecraft:*` tombstones, and raw `Enchantments` lists are automatically converted to the new `ItemEnchantments` + `minecraft:tooltip_display` data components. Hiding lore/enchantments/etc. now relies on component `show_in_tooltip` fields, keeping Raw Data exports free of negated component markers.

## Build from source

The project uses the [Architectury](https://github.com/architectury) toolchain.

```bash
git clone https://github.com/Rinorsi/CAD-Editor.git
cd CAD-Editor
chmod +x gradlew
./gradlew build
```

Artifacts will be under `fabric/build/libs` and `neoforge/build/libs`.
