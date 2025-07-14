package net.shoreline.client.impl.module.client;

import net.minecraft.client.gui.screen.ChatScreen;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.HudModule;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.gui.hud.HudGuiScreen;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class HudGuiModule extends ListeningToggleable
{
    public static HudGuiModule INSTANCE;

    public HudGuiModule()
    {
        super("HUD", "Heads up display", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (checkNull())
        {
            disable();
            return;
        }

        mc.setScreen(HudGuiScreen.INSTANCE);
    }

    @Override
    public void onDisable()
    {
        if (checkNull())
        {
            return;
        }

        mc.player.closeScreen();
    }

    @EventListener
    public void onHudOverlay(HudOverlayEvent.Post event)
    {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen))
        {
            return;
        }

        if (mc.options.hudHidden || mc.getDebugHud().shouldShowDebugHud())
        {
            return;
        }

        for (HudModule hudModule : Managers.MODULES.getHudModules())
        {
            if (hudModule.isEnabled())
            {
                hudModule.drawHudComponent(event.getContext(), event.getTickDelta());
            }
        }
    }
}
