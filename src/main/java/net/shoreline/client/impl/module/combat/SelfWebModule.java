package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.BlockPlacerModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.init.Managers;

public class SelfWebModule extends BlockPlacerModule {

    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates before placing the web", false));

    public SelfWebModule() {
        super("SelfWeb", "Places webs at the player's feet", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable() {
        final BlockPos pos = mc.player.getBlockPos();
        if (mc.world.getBlockState(pos).isAir()) {

            int slot = getBlockItemSlot(Blocks.COBWEB);
            if (slot == -1) {
                return;
            }
            Managers.INTERACT.placeBlock(pos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, (state, angles) ->
            {
                if (rotateConfig.getValue())
                {
                    if (state)
                    {
                        Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                    }
                    else
                    {
                        Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                    }
                }
            });
        }
        disable();
    }
}
