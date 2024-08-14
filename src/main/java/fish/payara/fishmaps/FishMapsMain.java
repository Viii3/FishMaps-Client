package fish.payara.fishmaps;

import fish.payara.fishmaps.config.Settings;
import fish.payara.fishmaps.messaging.Messenger;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FishMapsMain implements ModInitializer {
	public static final String MODID = "fishmaps";
    public static final Logger LOGGER = LoggerFactory.getLogger("FishMaps");

	@Override
	public void onInitialize () {
		Settings.read();
		ServerChunkEvents.CHUNK_LOAD.register(Messenger::cacheChunk);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Messenger.postFromCache();
			Messenger.resolveChunkCache();
		});
		ServerTickEvents.END_WORLD_TICK.register(Messenger::postAllPlayers);
	}
}