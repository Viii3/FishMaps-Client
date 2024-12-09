package fish.payara.fishmaps.messaging.payload;

import com.google.gson.Gson;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

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
