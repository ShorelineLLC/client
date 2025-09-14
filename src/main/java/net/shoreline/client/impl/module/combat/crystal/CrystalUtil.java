package net.shoreline.client.impl.module.combat.crystal;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionTrace;

import java.util.Set;

@UtilityClass
public class CrystalUtil
{
    public float getCrystalDamage(final BlockView blockView,
                                  final Vec3d pos,
                                  final Vec3d entityPos,
                                  final Box boundingBox,
                                  final boolean ignoreTerrain)
    {
        return ExplosionTrace.getDamageToPos(blockView,
                pos,
                entityPos,
                boundingBox,
                12.0f,
                ignoreTerrain);
    }

    public float getCrystalDamage(final BlockView blockView,
                                  final Vec3d pos,
                                  final Vec3d entityPos,
                                  final Box boundingBox,
                                  final boolean ignoreTerrain,
                                  final Set<BlockPos> ignoredBlocks)
    {
        return ExplosionTrace.getDamageToPos(blockView,
                pos,
                entityPos,
                boundingBox,
                12.0f,
                ignoreTerrain,
                ignoredBlocks);
    }
}
