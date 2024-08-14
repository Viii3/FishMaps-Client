package fish.payara.fishmaps;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class WorldUtil {
    public static BlockPos getTopmostBlock (int x, int z, BlockView blockView) {
        BlockPos pos = new BlockPos(x, blockView.getTopY(), z);
        while (blockView.getBlockState(pos).isAir() || blockView.getBlockState(pos).isOf(Blocks.BEDROCK)) {
            pos = pos.down();
        }
        return pos;
    }
}
