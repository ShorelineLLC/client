package net.shoreline.client.impl.module.misc;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.PlayerUtil;

public final class TestModule extends ToggleModule
{
    public TestModule()
    {
        super("Test", "in new york i milly rock", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    protected void onEnable()
    {
        if (mc.player != null)
        {
            for (int i = 0; i < 100; ++i)
            {
                final BlockPos pos = PlayerUtil.getRoundedBlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                Managers.INTERACT.placeBlockPacket(new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, false));
            }
        }

        toggle();
    }
}
