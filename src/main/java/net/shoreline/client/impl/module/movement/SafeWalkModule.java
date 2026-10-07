package net.shoreline.client.impl.module.movement;

import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class SafeWalkModule extends Toggleable
{
    Config<Boolean> grimConfig = new BooleanConfig.Builder("Grim")
            .setDescription("Allows you to clip at ledges on grim")
            .setDefaultValue(false).build();

    private boolean sneakOverride;

    public SafeWalkModule()
    {
        super("SafeWalk", "Prevents walking off edges", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onMove(PlayerMoveEvent event)
    {
        if (!mc.player.isOnGround())
        {
            return;
        }

        Vec3d move = event.getMovement();
        double x = move.x;
        double y = move.y;
        double z = move.z;

        double d = 0.05;
        while (x != 0.0 && isSpaceEmpty(x, -1.0, 0.0))
        {
            if (x < d && x >= -d)
            {
                x = 0.0;
                continue;
            }

            if (x > 0.0)
            {
                x -= d;
                continue;
            }

            x += d;
        }

        while (z != 0.0 && isSpaceEmpty(0.0, -1.0, z))
        {
            if (z < d && z >= -d)
            {
                z = 0.0;
                continue;
            }

            if (z > 0.0)
            {
                z -= d;
                continue;
            }

            z += d;
        }

        while (x != 0.0 && z != 0.0 && isSpaceEmpty(x, -1.0, z))
        {
            x = x < d && x >= -d ? 0.0 : (x > 0.0 ? x - d : x + d);
            if (z < d && z >= -d)
            {
                z = 0.0;
                continue;
            }

            if (z > 0.0)
            {
                z -= d;
                continue;
            }

            z += d;
        }

        if (grimConfig.getValue())
        {
            final Vec3d velocity = mc.player.getVelocity();
            final double deltaX = velocity.getX() - move.x;
            final double deltaZ = velocity.getZ() - move.z;

            sneakOverride = Math.abs(deltaX) > 9.0E-4 || Math.abs(deltaZ) > 9.0E-4;
            mc.options.sneakKey.setPressed(sneakOverride);
            return;
        }

        event.cancel();
        event.setMovement(new Vec3d(x, y, z));
    }

    private boolean isSpaceEmpty(double offsetX, double offsetY, double offsetZ)
    {
        return !mc.world.canCollide(mc.player, mc.player.getBoundingBox().offset(offsetX, offsetY, offsetZ));
    }
}
