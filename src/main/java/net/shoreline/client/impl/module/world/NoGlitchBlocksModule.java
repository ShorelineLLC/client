package net.shoreline.client.impl.module.world;

import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.BreakBlockEvent;
import net.shoreline.client.impl.event.network.InteractBlockEvent;
import net.shoreline.client.init.Managers;
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
    public void onInteractBlock(InteractBlockEvent event)
    {
        if (placeConfig.getValue() && mc.player.getStackInHand(event.getHand()).getItem() instanceof BlockItem && !mc.isInSingleplayer())
        {
            event.cancel();
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(event.getHand(), event.getHitResult(), id));
            mc.player.swingHand(event.getHand());
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
