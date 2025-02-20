package net.shoreline.client.impl.gui.click.impl;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.component.Button;
import net.shoreline.client.impl.gui.click.component.Frame;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

public class SearchButton extends Button
{
    public static String SEARCH_TEXT = null;

    private char[] buffer;
    private boolean typing;
    // Insertion point
    private boolean idling;
    private final Timer idleTimer = new CacheTimer();

    public SearchButton(Frame frame, float x, float y)
    {
        super(frame, x, y, 103.0f, 13.0f);
        buffer = "".toCharArray();
    }

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
    public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta)
    {
        setHeight(RenderManager.textHeight() + 4.0f);
        x = ix;
        y = iy;
        int unfilledColor = ClickGuiModule.getInstance().fixTransparency(0x33000000);
        fill(context, ix + 1.0f, iy, 1.0f, getHeight(), ClickGuiModule.getInstance().getColor(1.7f));
        fill(context, ix + getWidth() - 2.0f, iy, 1.0f, getHeight(), ClickGuiModule.getInstance().getColor(1.7f));
        fill(context, ix + 1.0f, iy - 1.0f, getWidth() - 2.0f, 1.0f, ClickGuiModule.getInstance().getColor(1.7f));
        fill(context, ix + 1.0f, iy + getHeight(), getWidth() - 2.0f, 1.0f, ClickGuiModule.getInstance().getColor(1.7f));
        rect(context, unfilledColor);

        // drawBorder(context, ix + (3.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (3.5f * ClickGuiModule.CLICK_GUI_SCALE), getWidth(), getHeight(), ClickGuiModule.getInstance().getColor());

        int whiteText = -1;
        String renderText = typing ? new String(buffer) + getInsertionPoint() : "Search...";
        drawStringScaled(context, renderText, ix + (5.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (3.5f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isWithin(mouseX, mouseY))
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1)
            {
                typing = !typing;
                if (!typing)
                {
                    SEARCH_TEXT = null;
                    buffer = "".toCharArray();
                }
                if (ClickGuiModule.getInstance().getSounds())
                {
                    Managers.SOUND.playSound(SoundManager.GUI_CLICK);
                }
            }
        }
        else
        {
            typing = false;
            SEARCH_TEXT = null;
            buffer = "".toCharArray();
        }
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
        if (typing)
        {
            switch (keyCode)
            {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_SPACE ->
                {
                    typing = false;
                    SEARCH_TEXT = null;
                    buffer = "".toCharArray();
                }
                case GLFW.GLFW_KEY_BACKSPACE ->
                {
                    if (buffer.length != 0)
                    {
                        buffer = ArrayUtils.remove(buffer, buffer.length - 1);
                        SEARCH_TEXT = new String(buffer);
                    }
                }
            }
        }
    }

    @Override
    public void charTyped(char character, int modifiers)
    {
        if (typing)
        {
            buffer = ArrayUtils.add(buffer, character);
            SEARCH_TEXT = new String(buffer);
        }
    }

    public boolean isTyping()
    {
        return typing;
    }

    public String getInsertionPoint()
    {
        if (idleTimer.passed(250))
        {
            idling = !idling;
            idleTimer.reset();
        }
        if (idling && typing)
        {
            return "_";
        }
        return "";
    }
}
