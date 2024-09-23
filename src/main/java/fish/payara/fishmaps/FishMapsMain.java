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
	private static final int THREADS = 1; // WARNING: Increasing this value can overload the Payara Server!

	public static Thread[] callThreads = new Thread[THREADS];
	private static final HttpCaller[] callers = new HttpCaller[THREADS];

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
			for (int i = 0; i < THREADS; ++i) {
				callers[i] = new HttpCaller();
				callThreads[i] = new Thread(null, callers[i], "Http thread " + i);
				callThreads[i].start();
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			for (HttpCaller caller : callers) {
				caller.stop();
			}

			for (Thread thread : callThreads) {
				try {
					thread.join();
				}
				catch (InterruptedException ignored) {

				}
			}
			Messenger.clear();
		});
	}
}