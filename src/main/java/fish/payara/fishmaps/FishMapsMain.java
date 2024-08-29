package fish.payara.fishmaps;

import fish.payara.fishmaps.config.Settings;
import fish.payara.fishmaps.messaging.Messenger;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FishMapsMain implements ModInitializer {
	public static final String MODID = "fishmaps";
    public static final Logger LOGGER = LoggerFactory.getLogger("FishMaps");
	public static Thread callThread1;
	public static Thread callThread2;

	private static final HttpCaller CALLER1 = new HttpCaller();
	private static final HttpCaller CALLER2 = new HttpCaller();

	@Override
	public void onInitialize () {
		Settings.read();
		ServerChunkEvents.CHUNK_LOAD.register(Messenger::cacheChunk);
		ServerTickEvents.END_SERVER_TICK.register(server -> Messenger.resolveChunkCache());
		ServerTickEvents.END_WORLD_TICK.register(Messenger::postAllPlayers);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("blockCache").executes(context -> {
				context.getSource().sendFeedback(() -> Text.literal("Blocks: " + Messenger.cacheSize()), false);
				return 1;
			}));
			dispatcher.register(CommandManager.literal("chunkCache").executes(context -> {
				context.getSource().sendFeedback(() -> Text.literal("Chunks: " + Messenger.chunkCacheSize()), false);
				return 1;
			}));
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			callThread1 = new Thread(null, CALLER1, "Http thread 1");
			callThread1.start();
			callThread2 = new Thread(null, CALLER2, "Http thread 2");
			callThread2.start();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			stopCallThreads();
			Messenger.clear();
		});
	}

	public static void stopCallThreads () {
		CALLER1.stop();
		CALLER2.stop();
		try {
			callThread1.join();
			callThread2.join();
		}
		catch (InterruptedException ignored) {

		}
	}
}