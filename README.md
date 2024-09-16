## FishMaps Client

The _client application_ for the [FishMaps web application](https://github.com/Viii3/FishMaps).  
This is a Minecraft server mod for Fabric feeds data about players and the world to the web application.

## Usage
The mod will cache all block updates and chunk loading in the world, and output blocks in groups of 256 to the web application.

### Commands
- `/blockCache`
  - Displays the number of blocks currently in the queue to be posted.
- `/chunkCache`
  - Displays the number of chunks currently in the queue to be resolved into blocks. 

## Setup
The default address is `localhost:8080`. After the first launch, the mod will create a config file where you may change the address of the web application.

## Building and Testing
- To build the project:
  - Run: `./gradlew build`
  - Find the jar file in `./build/libs/`.
- To run the project in a fully integrated test environment:
  - Run: `./gradlew runClient`
  - This will create and run a Minecraft installation with the mod installed.

The run configurations and available commands will be visible in IntelliJ and VSCode. Domain-specific commands will be under the "fabric" group.
