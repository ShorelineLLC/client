package net.shoreline.client.impl.world.explosion;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.world.AsyncWorldScanner;

import java.util.Set;

@RequiredArgsConstructor
public abstract class ExplosionScanner extends AsyncWorldScanner
{
    private final float explosionPower;

    public float getExplosionDamage(final Vec3d pos,
                                    final Vec3d entityPos,
                                    final Box boundingBox,
                                    final boolean ignoreTerrain)
    {
        return ExplosionTrace.getDamageToPos(this,
                pos,
                entityPos,
                boundingBox,
                explosionPower,
                ignoreTerrain);
    }

    public float getExplosionDamage(final Vec3d pos,
                                    final Vec3d entityPos,
                                    final Box boundingBox,
                                    final boolean ignoreTerrain,
                                    final Set<BlockPos> ignoredBlocks)
    {
        return ExplosionTrace.getDamageToPos(this,
                pos,
                entityPos,
                boundingBox,
                explosionPower,
                ignoreTerrain,
                ignoredBlocks);
    }
}
