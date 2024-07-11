package net.shoreline.client.impl.gui.click.component;

import net.minecraft.client.gui.DrawContext;

/**
 * @author linus
 * @since 1.0
 */
public class Frame extends Component implements Interactable
{
    //
    protected float px, py, fheight;

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Frame(float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        // fill(matrices, x, y, width, height, 0xe5000000);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {

    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {

    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {

    }

    @Override
    public void charTyped(char character, int modifiers)
    {

    }
}
