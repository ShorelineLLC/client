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

    // Config<Boolean> gradientConfig = register(new BooleanConfig("Gradient", "Adds a gradient to the elements", false));
    Config<Boolean> blurConfig = register(new BooleanConfig("Blur", "Adds a blur background to the panels", false));
    Config<Float> scaleConfig = register(new NumberConfig<>("Scale", "The gui scale", 0.5f, 1.0f, 3.0f));
    Config<Integer> scrollSpeedConfig = register(new NumberConfig<>("ScrollSpeed", "The speed of GUI scrolling", 5, 30, 100));
    Config<Boolean> soundsConfig = register(new BooleanConfig("Sounds", "Click sounds", true));
    Config<Boolean> descriptionsConfig = register(new BooleanConfig("Descriptions", "Shows feature descriptions", true));

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

    public int getColor(int a)
    {
        return ColorsModule.getInstance().getColor((int) (a * openCloseAnimation.getFactor())).getRGB();
    }

    public int getColor(float alpha)
    {
        return ColorsModule.getInstance().getColor((int) (100 * alpha * openCloseAnimation.getFactor())).getRGB();
    }

    public int getColor(int a, float alpha)
    {
        return ColorsModule.getInstance().getColor((int) (a * alpha * openCloseAnimation.getFactor())).getRGB();
    }

//    public int getGradient()
//    {
//        return gradientConfig.getValue() ? ColorsModule.getInstance().getGradient((int) (100 * openCloseAnimation.getFactor())).getRGB() : getColor();
//    }
//
//    public int getGradient(int a)
//    {
//        return gradientConfig.getValue() ? ColorsModule.getInstance().getGradient((int) (a * openCloseAnimation.getFactor())).getRGB() : getColor(a);
//    }
//
//    public int getGradient(float alpha)
//    {
//        return gradientConfig.getValue() ? ColorsModule.getInstance().getGradient((int) (100 * alpha * openCloseAnimation.getFactor())).getRGB() : getColor(alpha);
//    }
//
//    public int getGradient(int a, float alpha)
//    {
//        return gradientConfig.getValue() ? ColorsModule.getInstance().getGradient((int) (a * alpha * openCloseAnimation.getFactor())).getRGB() : getColor(a, alpha);
//    }

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

    public boolean getBlur()
    {
        return blurConfig.getValue();
    }

    public boolean getSounds()
    {
        return soundsConfig.getValue();
    }

    public boolean getDescriptions()
    {
        return descriptionsConfig.getValue();
    }

    public float getAlpha()
    {
        return (float) (transparencyAnimation.getFactor());
    }

    public float getScaleFactor()
    {
        return (float) (openCloseAnimation.getFactor());
    }

    public int getScrollSpeed()
    {
        return scrollSpeedConfig.getValue();
    }
}
