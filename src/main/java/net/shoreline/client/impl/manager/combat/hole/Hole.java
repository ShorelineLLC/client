package net.shoreline.client.impl.manager.combat.hole;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class Hole implements Position
{
    //
    private final List<BlockPos> holeOffsets;
    //
    private final BlockPos origin;
    private final HoleType safety;

    /**
     * @param origin
     * @param safety
     * @param holeOffsets
     */
    public Hole(BlockPos origin, HoleType safety, BlockPos... holeOffsets)
    {
        this.origin = origin;
        this.safety = safety;
        this.holeOffsets = Lists.newArrayList(holeOffsets);
        //
        this.holeOffsets.add(origin);
    }

    /**
     * @param entity
     * @return
     */
    public double squaredDistanceTo(Entity entity)
    {
        return entity.getEyePos().squaredDistanceTo(getCenter());
    }

    public boolean isStandard()
    {
        return holeOffsets.size() == 5;
    }

    public boolean isDouble()
    {
        return holeOffsets.size() == 8;
    }

    public boolean isDoubleX()
    {
        return isDouble() && holeOffsets.contains(origin.add(2, 0, 0));
    }

    public boolean isDoubleZ()
    {
        return isDouble() && holeOffsets.contains(origin.add(0, 0, 2));
    }

    public boolean isQuad()
    {
        return holeOffsets.size() == 12;
    }

    public HoleType getSafety()
    {
        return safety;
    }

    public BlockPos getPos()
    {
        return origin;
    }

    public List<BlockPos> getHoleOffsets()
    {
        return holeOffsets;
    }

    /**
     * @param off
     * @return
     */
    public boolean addHoleOffsets(BlockPos... off)
    {
        return holeOffsets.addAll(Arrays.asList(off));
    }

    /**
     * @return
     */
    public Vec3d getCenter()
    {
        BlockPos center;
        // This shit stupid
        if (isDoubleX())
        {
            center = origin.add(1, 0, 0);
        }
        else if (isDoubleZ())
        {
            center = origin.add(0, 0, -1);
        }
        else if (isQuad())
        {
            center = origin.add(1, 0, -1);
        }
        else
        {
            return origin.toCenterPos();
        }
        return Vec3d.of(center);
    }

    public Box getBoundingBox(double height)
    {
        Box render = null;
        if (getSafety() == HoleType.VOID)
        {
            render = new Box(getX(), getY(), getZ(), getX() + 1.0, getY() + 1.0, getZ() + 1.0);
        }
        else if (isDoubleX())
        {
            render = new Box(getX(), getY(), getZ(), getX() + 2.0, getY() + height, getZ() + 1.0);
        }
        else if (isDoubleZ())
        {
            render = new Box(getX(), getY(), getZ(), getX() + 1.0, getY() + height, getZ() + 2.0);
        }
        else if (isQuad())
        {
            render = new Box(getX(), getY(), getZ(), getX() + 2.0, getY() + height, getZ() + 2.0);
        }
        else if (isStandard())
        {
            render = new Box(getX(), getY(), getZ(), getX() + 1.0, getY() + height, getZ() + 1.0);
        }
        return render;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Hole hole)
        {
            return new HashSet<>(hole.getHoleOffsets()).containsAll(holeOffsets);
        }
        return false;
    }

    /**
     * Returns the X coordinate.
     */
    @Override
    public double getX()
    {
        return origin.getX();
    }

    /**
     * Returns the Y coordinate.
     */
    @Override
    public double getY()
    {
        return origin.getY();
    }

    /**
     * Returns the Z coordinate.
     */
    @Override
    public double getZ()
    {
        return origin.getZ();
    }
}
