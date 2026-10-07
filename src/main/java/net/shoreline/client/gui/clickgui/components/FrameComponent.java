package net.shoreline.client.gui.clickgui.components;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.Easing;

@Getter
@Setter
public abstract class FrameComponent extends DrawableComponent implements Interactable
{
    protected final Frame frame;

    // Positions inside the frame
    protected float x, y;

    protected float width;
    protected float height;

    protected float yOffset;

    protected final Animation hoverAnim;
    protected final Animation drawAnim;

    public FrameComponent(Frame frame,
                          float x,
                          float y,
                          float width,
                          float height)
    {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hoverAnim = new Animation(false, 150L, Easing.LINEAR);
        this.drawAnim = new Animation(true, 100L, Easing.LINEAR);
    }

    public void setHoverState(boolean state)
    {
        hoverAnim.setState(state);
    }

    public float getTx()
    {
        return frame.getX() + this.x;
    }

    public float getTy()
    {
        return frame.getY() + this.y + this.yOffset;
    }

    public float getDrawHeight()
    {
        return (float) (height * drawAnim.getFactor());
    }
}
