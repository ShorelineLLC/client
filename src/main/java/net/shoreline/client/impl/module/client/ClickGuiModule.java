package net.shoreline.client.impl.module.client;

import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @see ClickGuiScreen
 * @since 1.0
 */
public class ClickGuiModule extends ToggleModule
{

    private static ClickGuiModule INSTANCE;

    Config<Float> scaleConfig = register(new NumberConfig<>("Scale", "The gui scale", 0.5f, 1.0f, 3.0f));
    public Config<Boolean> underGlow = register(new BooleanConfig("UnderGlow", "GUI underglow", false));
    //    Config<Integer> hueConfig = register(new NumberConfig<>("Hue", "The saturation of colors", 0, 0, 360);
//    Config<Integer> saturationConfig = register(new NumberConfig<>("Saturation", "The saturation of colors", 0, 50, 100);
//    Config<Integer> brightnessConfig = register(new NumberConfig<>("Brightness", "The brightness of colors", 0, 50, 100);
//    Config<Integer> hue1Config = register(new NumberConfig<>("Hue1", "The saturation of colors", 0, 0, 360);
//    Config<Integer> saturation1Config = register(new NumberConfig<>("Saturation1", "The saturation of colors", 0, 50, 100);
//    Config<Integer> brightness1Config = register(new NumberConfig<>("Brightness1", "The brightness of colors", 0, 50, 100);
//    Config<Integer> alphaConfig = register(new NumberConfig<>("Alpha", "The alpha of colors", 0, 100, 100);
    //
    public static ClickGuiScreen CLICK_GUI_SCREEN;
    public static float CLICK_GUI_SCALE = 1.0f;
    private final Animation openCloseAnimation = new Animation(false, 400, Easing.BACK_OUT);
    private final Animation transparencyAnimation = new Animation(false, 300, Easing.CUBIC_IN_OUT);

    /**
     *
     */
    public ClickGuiModule()
    {
        super("ClickGui", "Opens the clickgui screen", ModuleCategory.CLIENT, GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
    }

    public static ClickGuiModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (mc.player == null || mc.world == null)
        {
            toggle();
            return;
        }
        // initialize the null gui screen instance
        if (CLICK_GUI_SCREEN == null)
        {
            CLICK_GUI_SCALE = scaleConfig.getValue();
            CLICK_GUI_SCREEN = new ClickGuiScreen(this);
            Shoreline.CONFIG.loadClickGui();
        }
        if (CLICK_GUI_SCALE != scaleConfig.getValue())
        {
            CLICK_GUI_SCALE = scaleConfig.getValue();
            CLICK_GUI_SCREEN = new ClickGuiScreen(this);
        }
        openCloseAnimation.setState(true);
        transparencyAnimation.setState(true);
        openCloseAnimation.reset();
        transparencyAnimation.reset();

        mc.setScreen(CLICK_GUI_SCREEN);
    }

    @Override
    public void onDisable()
    {
        if (mc.player == null || mc.world == null)
        {
            toggle();
            return;
        }
        if (CLICK_GUI_SCREEN != null)
        {
            Shoreline.CONFIG.saveClickGui();
        }
        mc.player.closeScreen();
        openCloseAnimation.setState(false);
        transparencyAnimation.setState(false);
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.POST
                && event.getConfig() == scaleConfig && mc.world == null)
        {
            CLICK_GUI_SCALE = scaleConfig.getValue();
        }
    }

    public int getColor()
    {
        return ColorsModule.getInstance().getColor((int) (100 * openCloseAnimation.getFactor())).getRGB();
    }

    public int getColor(float alpha)
    {
        return ColorsModule.getInstance().getColor((int) (100 * alpha * openCloseAnimation.getFactor())).getRGB();
    }

    // Applies a transparency to a color
    public int fixTransparency(int color)
    {
        float alpha = getAlpha();

        if (alpha == 1.0F)
        {
            return color;
        }

        float colorAlpha = (color >> 24) & 0xFF;

        alpha = Math.max(0.0F, Math.min(1.0F, alpha));

        int colorAlphaInt = Math.max(10, (int) (colorAlpha * alpha));

        return (colorAlphaInt << 24) | (color & 0xFFFFFF);
    }

    public float getAlpha()
    {
        return (float) (transparencyAnimation.getFactor());
    }

    public float getScaleFactor()
    {
        return (float) (openCloseAnimation.getFactor());
    }
}
