package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @since 1.0
 */
public class TextButton extends ConfigButton<String>
{

    private char[] buffer;
    private boolean typing;
    // Insertion point
    private boolean idling;
    private final Timer idleTimer = new CacheTimer();

    /**
     * @param frame
     * @param config
     */
    public TextButton(CategoryFrame frame, ModuleButton moduleButton, Config<String> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
        buffer = config.getValue().toCharArray();
    }

    /**
     * @param context
     * @param ix
     * @param iy
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta)
    {
        setHeight(RenderManager.textHeight() + 4.0f);
        x = ix;
        y = iy;
        int whiteText = -1;
        String renderText = typing ? new String(buffer) + getInsertionPoint() : config.getName() + Formatting.GRAY + " " + new String(buffer);
        drawStringScaled(context, renderText, ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);
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
                if (typing)
                {
                    config.setValue(new String(buffer));
                    typing = false;
                }
                else
                {
                    typing = true;
                }
            }
        }
        else
        {
            endTyping();
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
                case GLFW.GLFW_KEY_ENTER ->
                {
                    config.setValue(new String(buffer));
                    typing = false;
                }
                case GLFW.GLFW_KEY_BACKSPACE ->
                {
                    if (buffer.length != 0)
                    {
                        buffer = ArrayUtils.remove(buffer, buffer.length - 1);
                    }
                }
                case GLFW.GLFW_KEY_ESCAPE ->
                {
                    buffer = config.getValue().toCharArray();
                    typing = false;
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
        }
    }

    public void endTyping()
    {
        this.typing = false;
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
