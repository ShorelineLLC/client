package net.shoreline.client.impl.module.client;

import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.ClientColorEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class ColorsModule extends ConcurrentModule
{
    private static ColorsModule INSTANCE;

    //
    Config<Color> colorConfig = register(new ColorConfig("Global", "The primary client color", new Color(50, 100, 205), false, false));
    // Config<Color> gradientColorConfig = register(new ColorConfig("Gradient", "The secondary client color", new Color(0, 0, 165), false, false));

    /**
     *
     */
    public ColorsModule()
    {
        super("Colors", "Client color scheme", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static ColorsModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onClientColor(ClientColorEvent event)
    {
        event.setRgb(getRGB());
    }

    public Color getColor()
    {
        return colorConfig.getValue();
    }

    public Color getColor(float alpha)
    {
        ColorConfig config = (ColorConfig) colorConfig;
        return new Color(config.getRed() / 255.0f, config.getGreen() / 255.0f, config.getBlue() / 255.0f, MathHelper.clamp(alpha, 0.0f, 1.0f));
    }

    public Color getColor(int alpha)
    {
        ColorConfig config = (ColorConfig) colorConfig;
        return new Color(config.getRed(), config.getGreen(), config.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }

    public Integer getRGB()
    {
        return getColor().getRGB();
    }

    public int getRGB(int a)
    {
        return getColor(a).getRGB();
    }

//    public Color getGradient()
//    {
//        return gradientColorConfig.getValue();
//    }
//
//    public Color getGradient(float alpha)
//    {
//        ColorConfig config = (ColorConfig) gradientColorConfig;
//        return new Color(config.getRed() / 255.0f, config.getGreen() / 255.0f, config.getBlue() / 255.0f, MathHelper.clamp(alpha, 0.0f, 1.0f));
//    }
//
//    public Color getGradient(int alpha)
//    {
//        ColorConfig config = (ColorConfig) gradientColorConfig;
//        return new Color(config.getRed(), config.getGreen(), config.getBlue(), MathHelper.clamp(alpha, 0, 255));
//    }
//
//    public Integer getGradientRGB()
//    {
//        return getGradient().getRGB();
//    }
//
//    public int getGradientRGB(int a)
//    {
//        return getGradient(a).getRGB();
//    }
}
