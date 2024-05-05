package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @see ClickGuiScreen
 * @since 1.0
 */
public class ClickGuiModule extends ToggleModule {

//    Config<Integer> hueConfig = new NumberConfig<>("Hue", "The saturation of colors", 0, 0, 360);
//    Config<Integer> saturationConfig = new NumberConfig<>("Saturation", "The saturation of colors", 0, 50, 100);
//    Config<Integer> brightnessConfig = new NumberConfig<>("Brightness", "The brightness of colors", 0, 50, 100);
//    Config<Integer> hue1Config = new NumberConfig<>("Hue1", "The saturation of colors", 0, 0, 360);
//    Config<Integer> saturation1Config = new NumberConfig<>("Saturation1", "The saturation of colors", 0, 50, 100);
//    Config<Integer> brightness1Config = new NumberConfig<>("Brightness1", "The brightness of colors", 0, 50, 100);
//    Config<Integer> alphaConfig = new NumberConfig<>("Alpha", "The alpha of colors", 0, 100, 100);
    //
    public static ClickGuiScreen CLICK_GUI_SCREEN;
    private final Animation openCloseAnimation = new Animation(false, 300, Easing.CUBIC_IN_OUT);

    // TODO: Fix Gui scaling
    public float scaleConfig = 1.0f;

    /**
     *
     */
    public ClickGuiModule() {
        super("ClickGui", "Opens the clickgui screen", ModuleCategory.CLIENT,
                GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
        // initialize the null gui screen instance
        if (CLICK_GUI_SCREEN == null) {
            CLICK_GUI_SCREEN = new ClickGuiScreen(this);
        }
        mc.setScreen(CLICK_GUI_SCREEN);
        openCloseAnimation.setState(true);
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
        mc.player.closeScreen();
        openCloseAnimation.setState(false);
    }

    public int getColor() {
        return Modules.COLORS.getColor((int) (100 * openCloseAnimation.getFactor())).getRGB();
        // return ColorUtil.hslToColor(hueConfig.getValue(), saturationConfig.getValue(), brightnessConfig.getValue(), alphaConfig.getValue() / 100.0f).getRGB();
    }

    public int getColor1() {
        return Modules.COLORS.getColor((int) (100 * openCloseAnimation.getFactor())).getRGB();
        // return ColorUtil.hslToColor(hue1Config.getValue(), saturation1Config.getValue(), brightness1Config.getValue(), alphaConfig.getValue() / 100.0f).getRGB();
    }

    public int getColor(float alpha) {
        return Modules.COLORS.getColor((int) (100 * alpha * openCloseAnimation.getFactor())).getRGB();
        // return ColorUtil.hslToColor(hueConfig.getValue(), saturationConfig.getValue(), brightnessConfig.getValue(), MathHelper.clamp(alphaConfig.getValue() * alpha / 100.0f, 0.0f, 1.0f)).getRGB();
    }

    public int getColor1(float alpha) {
        return Modules.COLORS.getColor((int) (100 * alpha * openCloseAnimation.getFactor())).getRGB();
        // return ColorUtil.hslToColor(hue1Config.getValue(), saturation1Config.getValue(), brightness1Config.getValue(), MathHelper.clamp(alphaConfig.getValue() * alpha / 100.0f, 0.0f, 1.0f)).getRGB();
    }

    /**
     * @return
     */
    public Float getScale() {
        return scaleConfig;
    }
}
