# EasyOP

[![License](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.8+-blue.svg)
![Fabric](https://img.shields.io/badge/Loader-Fabric-orange.svg)

EasyOP is a Fabric-based Minecraft mod that provides an intuitive in-game control panel for server administrators and single-player world owners. It simplifies common management tasks through a graphical user interface (HUD).

## Features

- **In-Game HUD**: Access all management functions via a dedicated HUD (Default key: `Left Alt`, configurable in Controls).
- **Fast Settings**:
  - **Time Control**: One-click to set time to Sunrise, Day, Noon, Sunset, Night, or Midnight.
  - **Locate**: Easily find nearby structures (Villages, Fortresses, Mansions, etc.), biomes (Deserts), and POIs (Librarians).
  - **Mob Spawning**: Quickly toggle natural mob spawning.
- **Player List**:
  - View all online players.
  - **Kick**: Disconnect players with a single click.
  - **Teleport (TP)**: Teleport directly to any online player.
- **Game Rules Management**:
  - A scrollable list of all Minecraft game rules.
  - Includes detailed descriptions in both Chinese and English.
  - Support for Boolean rules (True/False buttons) and Integer rules (Text input directly on HUD).
- **Fully Localized**: Complete support for Simplified Chinese and English.

## Installation

1. Make sure you have the [Fabric Loader](https://fabricmc.net/use/) installed.
2. Download the mod and place it in your `.minecraft/mods` folder.
3. Ensure you have the [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) installed.

## Usage

- Press `Left Alt` (default) to open/close the EasyOP panel.
- Use the sidebar to switch between **Fast Settings**, **Player List**, and **Game Rules**.
- In the Game Rules tab, use the mouse wheel to scroll through the list.
- To edit an integer game rule, click "EDIT", type the number, and press "Enter".

## License

This project is licensed under the MPL-2.0 License.
