package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.gui.click.component.Button;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;

/**
 * @param <T>
 * @author linus
 * @since 1.0
 */
public abstract class ConfigButton<T> extends Button
{
    //
    protected final Config<T> config;
    protected final ModuleButton moduleButton;

    /**
     * @param frame
     * @param config
     */
    public ConfigButton(CategoryFrame frame, ModuleButton moduleButton, Config<T> config, float x, float y)
    {
        super(frame, x, y, 99.0f, 13.0f);
        this.moduleButton = moduleButton;
        this.config = config;
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, float mouseX, float mouseY, float delta)
    {
        render(context, x, y, mouseX, mouseY, delta);
    }

    /**
     * @param context
     * @param ix
     * @param iy
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    public abstract void render(DrawContext context, float ix, float iy,
                                float mouseX, float mouseY, float delta);

    /**
     * @return
     */
    public Config<T> getConfig()
    {
        return config;
    }
}
