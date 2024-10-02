## FishMaps Client

The _client application_ for the [FishMaps web application](https://github.com/Viii3/FishMaps).  
This is a Minecraft server mod for Fabric. It feeds data about players and the world to the web application to populate the map.

## Usage
The mod will cache all block updates and chunk loading in the world, and output blocks in groups of 256 to the web application.

### In-Game Commands
- `/blockCache`
  - Displays the number of blocks currently in the queue to be posted.
- `/chunkCache`
  - Displays the number of chunks currently in the queue to be resolved into blocks. 

## Setup
By default, the mod will post data to `localhost:8080`.  
This address can be modified via the config file `/path/to/your/minecraft/profile/.../config/fishmaps.json`.

After the first launch, the mod will create the config file, it is read only once at the beginning of each launch.

## Building and Testing
- To build the project:
  - Run: `./gradlew build`
  - Find the jar file in `./build/libs/`.
- To run the project in a fully integrated test environment:
  - Run: `./gradlew runClient`
  - This will create and run a Minecraft installation with the mod installed.
  - The Minecraft profile for the test environment will be the `./run/` folder.

The run configurations and available commands will be visible in IntelliJ and VSCode. Domain-specific commands will be under the "fabric" group.
 
## Installation
> [!NOTE]
> The mod is intended for servers, but will also work in singleplayer. You do not need the mod on both the client and server, only the server.

This step refers to using FishMaps Client in a production environment. If using the integrated test environment via gradle, then this step is done automatically as part of `./gradle runClient`.

### Minecraft
- Install Minecraft
- Install [Fabric](https://fabricmc.net/) and allow it to create a new profile.
  - You can also manually create a Fabric profile or use an existing one, but these steps assume first-time setup.
- Launch the game with the Fabric profile, this will create the necessary folder structure.
- Close the game.
- Open the folder of the Minecraft profile, this is accessible via the Minecraft launcher.
- Place the JAR file into the `mods` folder.

The mod will be active when you next launch the game and enter a singleplayer world.

### Minecraft Server
- Download the [Fabric](https://fabricmc.net/) Minecraft server file.
- Launch the Fabric server, this will perform all required folder setup in the current directory.
- Close the server.
- Place the JAR file into the `mods` folder.

The mod will be active when you next launch the Minecraft server.
