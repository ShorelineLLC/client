package net.shoreline.client.gui.clickgui.components;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;

@Getter
@Setter
public abstract class FrameComponent extends DrawableComponent implements Interactable
{
    protected final Frame frame;

    // Positions inside the frame
    protected int x, y;

    protected int width;
    protected int height;

    protected int yOffset;

    protected final Animation hoverAnim;

    public FrameComponent(Frame frame,
                          int x,
                          int y,
                          int width,
                          int height)
    {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hoverAnim = new Animation(false, 150L, Easing.LINEAR);
    }

    public int getTx()
    {
        return frame.getX() + this.x;
    }

    public int getTy()
    {
        return frame.getY() + this.y + this.yOffset;
    }
}
