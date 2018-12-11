package unknowndomain.engine.world.chunk;

import unknowndomain.engine.RuntimeObject;
import unknowndomain.engine.block.Block;
import unknowndomain.engine.entity.Entity;
import unknowndomain.engine.math.BlockPos;
import unknowndomain.engine.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public interface Chunk extends RuntimeObject {

    int DEFAULT_X_SIZE = 16;
    int DEFAULT_Y_SIZE = 16;
    int DEFAULT_Z_SIZE = 16;

    /**
     * Get block in a specific path
     *
     * @param x x-coordinate of the block related to chunk coordinate system
     * @param y y-coordinate of the block related to chunk coordinate system
     * @param z z-coordinate of the block related to chunk coordinate system
     * @return the block in the specified path
     */
    Block getBlock(int x, int y, int z);

    @Nonnull
    List<Entity> getEntities();

    default Block getBlock(BlockPos pos) {
        return getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Set block in a specific path
     *
     * @param x x-coordinate of the block related to chunk coordinate system
     * @param y y-coordinate of the block related to chunk coordinate system
     * @param z z-coordinate of the block related to chunk coordinate system
     */
    Block setBlock(int x, int y, int z, Block destBlock);

    default Block setBlock(BlockPos pos, Block destBlock) {
        return setBlock(pos.getX(), pos.getY(), pos.getZ(), destBlock);
    }

    World getWorld();
}
