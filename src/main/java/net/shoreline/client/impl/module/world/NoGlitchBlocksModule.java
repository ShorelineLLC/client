package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.BreakBlockEvent;
import net.shoreline.client.impl.event.world.SetBlockStateEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class NoGlitchBlocksModule extends ToggleModule
{
    Config<Boolean> placeConfig = register(new BooleanConfig("Place", "Places blocks only after the server confirms", false));
    Config<Boolean> destroyConfig = register(new BooleanConfig("Destroy", "Destroys blocks only after the server confirms", false));

    public NoGlitchBlocksModule()
    {
        super("NoGlitchBlocks", "Prevents blocks from being glitched in the world", ModuleCategory.WORLD);
    }

    /**
     * @param event
     */
    @EventListener
    public void onSetBlockState(SetBlockStateEvent event)
    {
        if (placeConfig.getValue() && event.getFlags() != (Block.NOTIFY_ALL | Block.FORCE_STATE) && !mc.isInSingleplayer())
        {
            event.cancel();
        }
    }

    /**
     * @param event
     */
    @EventListener
    public void onBreakBlock(BreakBlockEvent event)
    {
        if (destroyConfig.getValue() && !mc.isInSingleplayer())
        {
            event.cancel();
        }
    }
}
