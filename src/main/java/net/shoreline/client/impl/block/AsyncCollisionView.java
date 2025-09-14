package net.shoreline.client.impl.block;

import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AsyncCollisionView extends AsyncBlockView implements CollisionView
{
    private final WorldBorder border = new WorldBorder();

    /** May potentially be unsafe calling {@link net.minecraft.block.EntityShapeContext} **/
    @Override
    public Iterable<VoxelShape> getBlockCollisions(Entity entity, Box box)
    {
        return () -> new AsyncCollisionIterator(ShapeContext.of(entity), box);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(Entity var1, Box var2)
    {
        return new ArrayList<>();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return border;
    }

    @Nullable
    public BlockView getChunkAsView(int var1, int var2)
    {
        return this;
    }

    public VoxelShape getCollisionShape(ShapeContext context,
                                        BlockState state,
                                        BlockPos pos)
    {
        return state.getCollisionShape(this, pos, context);
    }

    public class AsyncCollisionIterator extends AbstractIterator<VoxelShape>
    {
        private final Box box;
        private final ShapeContext context;
        private final CuboidBlockIterator blockIterator;
        private final BlockPos.Mutable pos;
        private final VoxelShape boxShape;

        public AsyncCollisionIterator(ShapeContext context, Box box)
        {
            this.context = context;
            this.pos = new BlockPos.Mutable();
            this.boxShape = VoxelShapes.cuboid(box);
            this.box = box;
            int i = MathHelper.floor(box.minX - 1.0e-7) - 1;
            int j = MathHelper.floor(box.maxX + 1.0e-7) + 1;
            int k = MathHelper.floor(box.minY - 1.0e-7) - 1;
            int l = MathHelper.floor(box.maxY + 1.0e-7) + 1;
            int m = MathHelper.floor(box.minZ - 1.0e-7) - 1;
            int n = MathHelper.floor(box.maxZ + 1.0e-7) + 1;
            blockIterator = new CuboidBlockIterator(i, k, m, j, l, n);
        }

        @Override
        protected VoxelShape computeNext()
        {
            while (blockIterator.step()) 
            {
                int i = blockIterator.getX();
                int j = blockIterator.getY();
                int k = blockIterator.getZ();
                int l = blockIterator.getEdgeCoordinatesCount();
                if (l == 3)
                {
                    continue;
                }
                
                pos.set(i, j, k);
                BlockState blockState = getBlockState(pos);
                VoxelShape voxelShape = getCollisionShape(context, blockState, pos);
                if (voxelShape == VoxelShapes.fullCube())
                {
                    if (!box.intersects(i, j, k, (double) i + 1.0, (double) j + 1.0, (double) k + 1.0))
                    {
                        continue;
                    }

                    return voxelShape.offset(i, j, k);
                }

                VoxelShape voxelShape2 = voxelShape.offset(i, j, k);
                if (voxelShape2.isEmpty() || !VoxelShapes.matchesAnywhere(voxelShape2, boxShape, BooleanBiFunction.AND))
                {
                    continue;
                }

                return voxelShape2;
            }

            return endOfData();
        }
    }
}
