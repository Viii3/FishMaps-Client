package fish.payara.fishmaps.messaging;

import com.google.gson.Gson;
import fish.payara.fishmaps.FishMapsMain;
import fish.payara.fishmaps.WorldUtil;
import fish.payara.fishmaps.config.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Messenger {
    private static final Queue<BlockData> CACHE = new LinkedList<>();
    private static final Queue<CachedChunk> CHUNK_CACHE = new LinkedList<>();

    private static String blockPost = Settings.getAddress("/api/block");
    private static String playerPost = Settings.getAddress("/api/player");

    public static void updateAddresses () {
        blockPost = Settings.getAddress("/api/block");
        playerPost = Settings.getAddress("/api/player");
    }

    public static void postAllPlayers (ServerWorld world) {
        HttpClient client = HttpClient.newHttpClient();
        world.getPlayers(player -> !player.isSpectator()).forEach(player -> postPlayer(player, client));
        client.close();
    }

    public static void postPlayer (PlayerEntity player, HttpClient client) {
        PlayerData data = PlayerData.fromPlayer(player);
        HttpRequest post = HttpRequest.newBuilder(URI.create(playerPost))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(data.toJSON()))
            .build();

        client.sendAsync(post, HttpResponse.BodyHandlers.discarding());
    }

    public static void postBlock (BlockData block) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest post = HttpRequest.newBuilder(URI.create(blockPost))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(block.toJSON()))
            .build();

        client.sendAsync(post, HttpResponse.BodyHandlers.discarding());
        client.close();
    }

    public static void postBlock (BlockState blockState, BlockPos pos, World world) {
        postBlock(BlockData.fromBlockState(blockState, pos, world.getDimensionEntry().getIdAsString()));
    }

    public static void cacheBlockUpdate (BlockData block) {
        CACHE.add(block);
    }

    public static void cacheBlockUpdate (BlockState blockState, BlockPos pos, World world) {
        cacheBlockUpdate(BlockData.fromBlockState(blockState, pos, world.getDimensionEntry().getIdAsString()));
    }

    public static void cacheChunk (World world, Chunk chunk) {
        CHUNK_CACHE.add(new CachedChunk(chunk.getPos(), world));
    }

    public static void resolveChunkCache () {
        if (!CHUNK_CACHE.isEmpty()) {
            CachedChunk cached = CHUNK_CACHE.poll();
            if (!cached.world.isChunkLoaded(cached.chunkPos.x, cached.chunkPos.z)) return;

            for (int x = cached.chunkPos.getStartX(); x <= cached.chunkPos.getEndX(); ++x) {
                for (int z = cached.chunkPos.getStartZ(); z <= cached.chunkPos.getEndZ(); ++z) {
                    BlockPos pos = cached.world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).down();
                    states.add(BlockData.fromBlockState(cached.world.getBlockState(pos), pos, cached.world));
                }
            }
        }
    }

    public static void postFromCache (int posts) {
        for (int i = 0; i < posts; ++i) {
            if (!CACHE.isEmpty()) postBlock(CACHE.poll());
        }
    }

    public static void postFromCache () {
        postFromCache(1);
    }

    public record BlockData (int x, int y, int z, int colour, String dimension) {
        private static final Gson gson = new Gson();

        public static BlockData fromBlockState (BlockState blockState, BlockPos pos, String dimension) {
            return new BlockData(pos.getX(), pos.getY(), pos.getZ(), blockState.getBlock().getDefaultMapColor().color, dimension);
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
