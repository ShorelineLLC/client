package net.shoreline.client.gui.clickgui.config.picker;

import lombok.Setter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.Easing;

public abstract class ExpandableComponent<T> extends ConfigComponent<T>
{
    @Setter
    protected boolean pickerOpen;
    protected final Animation collapseAnim;

    public ExpandableComponent(Config<T> config,
                               ModuleComponent moduleComponent,
                               Frame frame,
                               float x,
                               float y,
                               float frameWidth,
                               float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.collapseAnim = new Animation(false, 200, Easing.CUBIC_OUT);
    }

    public abstract float getComponentHeight();
}
