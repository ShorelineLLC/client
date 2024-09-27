package net.shoreline.client.impl.gui.click.component;

import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;

/**
 * @author linus
 * @since 1.0
 */
public abstract class Button extends Component implements Interactable
{
    //
    protected final Frame frame;
    protected final Animation hoverAnimation = new Animation(false, 150, Easing.LINEAR);

    /**
     * @param frame
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Button(Frame frame, float x, float y, float width, float height)
    {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * @param frame
     */
    public Button(Frame frame)
    {
        this.frame = frame;
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public abstract void mouseClicked(double mouseX, double mouseY, int button);

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public abstract void mouseReleased(double mouseX, double mouseY, int button);

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public abstract void keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * @return
     */
    public Frame getFrame()
    {
        return frame;
    }
}
