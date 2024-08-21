package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.player.PushEntityEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @see FakePlayerEntity
 * @since 1.0
 */
public class FakePlayerModule extends ToggleModule
{
    //
    private FakePlayerEntity fakePlayer;

    /**
     *
     */
    public FakePlayerModule()
    {
        super("FakePlayer", "Spawns an indestructible client-side player",
                ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (mc.player != null && mc.world != null)
        {
            fakePlayer = new FakePlayerEntity(mc.player, "FakePlayer");
            fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable()
    {
        if (fakePlayer != null)
        {
            fakePlayer.despawnPlayer();
            fakePlayer = null;
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        fakePlayer = null;
        disable();
    }

    @EventListener
    public void onPushEntity(PushEntityEvent event)
    {
        // Prevents Simulation flags (as the FakePlayer is client only, so Grim rightfully
        // flags us for that push motion that shouldn't happen
        if (event.getPushed().equals(mc.player) && event.getPusher().equals(fakePlayer))
        {
            event.setCanceled(true);
        }
    }
}
