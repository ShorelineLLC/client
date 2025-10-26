package net.shoreline.client.impl.module.client;

import net.minecraft.client.gui.screen.ChatScreen;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.gui.hud.HudGuiScreen;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class HudGuiModule extends ListeningToggleable
{
    public static HudGuiModule INSTANCE;
    Config<Float> scaleConfig = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.5f).setMax(1.5f).setDefaultValue(1.0f)
            .setDescription("The hud element scale").build();
    Config<ColorEffect> effectConfig = new EnumConfig.Builder<ColorEffect>("Effects")
            .setValues(ColorEffect.values())
            .setDefaultValue(ColorEffect.NONE)
            .setDescription("Applies effects to colors in hud.").build();
    Config<Float> rainbowSpeed = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.0f).setMax(2.0f).setDefaultValue(1.0f)
            .setVisible(() -> effectConfig.getValue() != ColorEffect.NONE).build();
    Config<Float> pulseDepth = new NumberConfig.Builder<Float>("Depth")
            .setMin(0.0f).setMax(2.0f).setDefaultValue(1.0f)
            .setVisible(() -> effectConfig.getValue() != ColorEffect.NONE).build();
    Config<Float> factorConfig = new NumberConfig.Builder<Float>("Factor")
            .setMin(0.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setVisible(() -> effectConfig.getValue() != ColorEffect.NONE).build();

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

        ClickGuiModule.INSTANCE.disable();
        ClickGuiModule.INSTANCE.setFadeState(true);
        mc.setScreen(HudGuiScreen.INSTANCE);
    }

    @Override
    public void onDisable()
    {
        if (checkNull())
        {
            return;
        }

        ClickGuiModule.INSTANCE.setFadeState(false);
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

        HudGuiScreen.INSTANCE.runAnchorTick(true);
        for (HudModule hudModule : Managers.MODULES.getHudModules())
        {
            if (hudModule.isEnabled())
            {
                hudModule.drawHudComponent(event.getContext(), event.getTickDelta());
            }
        }
    }

    public int getColor(int y)
    {
        y *= factorConfig.getValue();
        int primary = ThemeModule.INSTANCE.primaryColor.getValue().getRGB();
        return switch (effectConfig.getValue())
        {
            case NONE -> primary;
            case RAINBOW -> Color.HSBtoRGB(ColorUtil.getRainbowHue(rainbowSpeed.getValue(), y), 1.0f, 1.0f);
            case PULSE -> ColorUtil.getPulse(rainbowSpeed.getValue(), pulseDepth.getValue(), y * 10, primary);
        };
    }

    public enum ColorEffect
    {
        NONE,
        RAINBOW,
        PULSE
    }
}
