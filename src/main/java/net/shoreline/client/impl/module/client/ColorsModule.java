package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.ClientColorEvent;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class ColorsModule extends ConcurrentModule {
    private static ColorsModule INSTANCE;

    //
    Config<Color> colorConfig = new ColorConfig("Color", "The primary client color", new Color(255, 0, 0), false, false);
    // Config<Color> color1Config = new ColorConfig("Accent-Color", "The accent client color", new Color());
    Config<Boolean> rainbowConfig = new BooleanConfig("Rainbow", "Renders rainbow colors for modules", false);

    /**
     *
     */
    public ColorsModule() {
        super("Colors", "Client color scheme", ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static ColorsModule getInstance() {
        return INSTANCE;
    }

    @EventListener
    public void onClientColor(ClientColorEvent event) {
        event.setRgb(getRGB());
    }

    public Color getColor() {
        return colorConfig.getValue();
    }

    public Color getColor(float alpha) {
        ColorConfig config = (ColorConfig) colorConfig;
        return new Color(config.getRed() / 255.0f, config.getGreen() / 255.0f, config.getBlue() / 255.0f, alpha);
    }

    public Color getColor(int alpha) {
        ColorConfig config = (ColorConfig) colorConfig;
        return new Color(config.getRed(), config.getGreen(), config.getBlue(), alpha);
    }

    public Integer getRGB() {
        return getColor().getRGB();
    }

    public int getRGB(int a) {
        return getColor(a).getRGB();
    }
}
