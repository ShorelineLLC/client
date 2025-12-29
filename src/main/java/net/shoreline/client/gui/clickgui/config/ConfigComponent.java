package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.gui.clickgui.ModuleComponent;

@Getter
public abstract class ConfigComponent<T> extends FrameComponent
{
    private final Config<T> config;
    private final ModuleComponent moduleComponent;

    @Setter
    private float moduleOffset;

    public ConfigComponent(Config<T> config,
                           ModuleComponent moduleComponent,
                           Frame frame,
                           float x,
                           float y,
                           float frameWidth,
                           float frameHeight)
    {
        super(frame, x, y, frameWidth, frameHeight);
        this.config = config;
        this.moduleComponent = moduleComponent;
        getDrawAnim().setStateHard(config.isVisible());
        config.addObserver(this::onConfigUpdate);
    }

    protected void onConfigUpdate(T value) {}

    @Override
    public float getTx()
    {
        return getModuleComponent().getTx();
    }

    @Override
    public float getTy()
    {
        ModuleComponent parent = getModuleComponent();
        return parent.getTy() + parent.getHeight() + this.y + this.yOffset;
    }

    public void reset()
    {
        /* Implemented by the Component */
    }
}
