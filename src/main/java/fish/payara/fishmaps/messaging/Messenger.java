package fish.payara.fishmaps.messaging;

import com.google.gson.Gson;
import fish.payara.fishmaps.config.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Messenger {
    private static final String BLOCK_ADDRESS = "/api/map/block";
    private static final String BLOCK_LIST_ADDRESS = "/api/map/block/multiple";
    private static final String PLAYER_ADDRESS = "/api/player";

    private static final Queue<BlockData> CACHE = new ConcurrentLinkedQueue<>();
    private static final Queue<CachedChunk> CHUNK_CACHE = new LinkedList<>();

    private static String blockPost = Settings.getAddress(BLOCK_ADDRESS);
    private static String blockListPost = Settings.getAddress(BLOCK_LIST_ADDRESS);
    private static String playerPost = Settings.getAddress(PLAYER_ADDRESS);

    public static void updateAddresses () {
        blockPost = Settings.getAddress(BLOCK_ADDRESS);
        blockListPost = Settings.getAddress(BLOCK_LIST_ADDRESS);
        playerPost = Settings.getAddress(PLAYER_ADDRESS);
    }

    public static void postAllPlayers (ServerWorld world) {
        HttpClient client = HttpClient.newHttpClient();
        world.getPlayers(player -> !player.isSpectator() && player.age % 100 == 0).forEach(player -> postPlayer(player, client));
        client.close();
    }

    public static void postPlayer (PlayerEntity player, HttpClient client) {
        PlayerData data = PlayerData.fromPlayer(player);
        HttpRequest post = HttpRequest.newBuilder(URI.create(playerPost))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(data.toJSON()))
            .timeout(Duration.ofMillis(100))
            .build();

        client.sendAsync(post, HttpResponse.BodyHandlers.discarding());
    }

    public static void postBlock (BlockData block, HttpClient client) {
        HttpRequest post = HttpRequest.newBuilder(URI.create(blockPost))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(block.toJSON()))
            .timeout(Duration.ofMillis(1000))
            .build();

        try {
            client.send(post, HttpResponse.BodyHandlers.discarding());
        }
        catch (Exception ignored) {

        }
    }

    public static void postBlock (List<BlockData> blocks, HttpClient client) {
        HttpRequest post = HttpRequest.newBuilder(URI.create(blockListPost))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(BlockData.listJSON(blocks)))
            .timeout(Duration.ofMillis(4000))
            .build();

        try {
            client.send(post, HttpResponse.BodyHandlers.discarding());
        }
        catch (Exception ignored) {

        }
    }

    public static void cacheBlockUpdate (BlockData block) {
        CACHE.add(block);
    }

    public static void cacheBlockUpdates (Collection<BlockData> data) {
        CACHE.addAll(data);
    }

    public static void cacheBlockUpdate (BlockState blockState, BlockPos pos, World world) {
        cacheBlockUpdate(BlockData.fromBlockState(blockState, pos, world));
    }

    public static void cacheChunk (World world, Chunk chunk) {
        CHUNK_CACHE.add(new CachedChunk(chunk.getPos(), world));
    }

    public static void clear () {
        CACHE.clear();
        CHUNK_CACHE.clear();
    }

    public static int cacheSize () {
        return CACHE.size();
    }

    public static int chunkCacheSize () {
        return CHUNK_CACHE.size();
    }

    public static void resolveChunkCache () {
        if (!CHUNK_CACHE.isEmpty()) {
            CachedChunk cached = CHUNK_CACHE.poll();
            if (!cached.world.isChunkLoaded(cached.chunkPos.x, cached.chunkPos.z)) return;

            List<BlockData> states = new ArrayList<>();
            for (int x = cached.chunkPos.getStartX(); x <= cached.chunkPos.getEndX(); ++x) {
                for (int z = cached.chunkPos.getStartZ(); z <= cached.chunkPos.getEndZ(); ++z) {
                    BlockPos pos = cached.world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
                    states.add(BlockData.fromBlockState(cached.world.getBlockState(pos), pos, cached.world));
                }
            }
            cacheBlockUpdates(states);
        }
    }

    public static void postFromCache (HttpClient client) {
        if (!CACHE.isEmpty()) postBlock(CACHE.poll(), client);
    }

    public static void postFromCache (HttpClient client, int maxBlocks) {
        List<BlockData> blocksToPost = new ArrayList<>();
        for (int i = 0; i < maxBlocks && !CACHE.isEmpty(); ++i) {
            blocksToPost.add(CACHE.poll());
        }
        postBlock(blocksToPost, client);
    }

    public record BlockData (int x, int y, int z, int colour, String dimension) {
        private static final Gson gson = new Gson();

        public static BlockData fromBlockState (BlockState blockState, BlockPos pos, World world) {
            return new BlockData(pos.getX(), pos.getY(), pos.getZ(), blockState.getBlock().getDefaultMapColor().color, world.getDimensionEntry().getIdAsString());
        }

        public static String listJSON (List<BlockData> list) {
            return gson.toJson(list);
        }

        public String toJSON () {
            return gson.toJson(this);
        }
    }

    public record PlayerData (String name, int x, int z, String dimension) {
        private static final Gson gson = new Gson();

        public static PlayerData fromPlayer (PlayerEntity player) {
            return new PlayerData(player.getName().getString(), player.getBlockX(), player.getBlockZ(), player.getWorld().getDimensionEntry().getIdAsString());
        }

        public String toJSON () {
            return gson.toJson(this);
        }
    }

    record CachedChunk (ChunkPos chunkPos, World world) {

    }
}
