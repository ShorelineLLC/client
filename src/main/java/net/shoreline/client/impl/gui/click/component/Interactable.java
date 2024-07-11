package net.shoreline.client.impl.gui.click.component;

/**
 * @author linus
 * @see Drawable
 * @since 1.0
 */
public interface Interactable extends Drawable
{
    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    void mouseClicked(double mouseX, double mouseY, int button);

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    void mouseReleased(double mouseX, double mouseY, int button);

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    void keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     *
     * @param character
     * @param modifiers
     */
    void charTyped(char character, int modifiers);
}
