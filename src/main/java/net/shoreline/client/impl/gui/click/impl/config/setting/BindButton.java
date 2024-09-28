package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.init.Managers;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author linus
 * @since 1.0
 */
public class BindButton extends ConfigButton<Macro>
{
    // Check for whether we are listening for an input
    private boolean listening;

    /**
     * @param frame
     * @param config
     * @param x
     * @param y
     */
    public BindButton(CategoryFrame frame, ModuleButton moduleButton, Config<Macro> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
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
    public void render(DrawContext context, float ix, float iy, float mouseX,
                       float mouseY, float delta)
    {
        setHeight(RenderManager.textHeight() + 4.0f);

        x = ix;
        y = iy;
        final Macro macro = config.getValue();
        String val = listening ? "..." : macro.getKeyName();

        boolean state = isWithin(mouseX, mouseY);
        if (state != hoverAnimation.getState())
        {
            hoverAnimation.setState(state);
        }
        int hoverAlpha = (int) (80 * MathHelper.clamp(hoverAnimation.getFactor(), 0.0f, 1.0f));

        rect(context, new Color(hoverAlpha, hoverAlpha, hoverAlpha, hoverAlpha).getRGB());

        int whiteText = -1;
        drawStringScaled(context, config.getName(), ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

        float textLeng = RenderManager.textWidth(config.getName()) * ClickGuiModule.CLICK_GUI_SCALE;

        int grayText = 0xFFAAAAAA;
        drawStringScaled(context, " " + val, ix + (2.0F * ClickGuiModule.CLICK_GUI_SCALE) + textLeng, iy + (4.0F * ClickGuiModule.CLICK_GUI_SCALE), grayText);
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
            if (button == GLFW_MOUSE_BUTTON_1)
            {
                listening = !listening;
            }
            else if (button == GLFW_MOUSE_BUTTON_2 && !listening)
            {
                // Reset the bind
                ((MacroConfig) config).setValue(GLFW_KEY_UNKNOWN);
            }
            else
            {
                if (listening)
                {
                    // Ignore Right click
                    if (button != GLFW_MOUSE_BUTTON_2)
                    {
                        // Mouse bind
                        ((MacroConfig) config).setValue(1000 + button);
                    }
                    listening = false;
                }
            }

            if (ClickGuiModule.getInstance().getSounds())
            {
                Managers.SOUND.playSound(SoundManager.GUI_CLICK);
            }
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
        if (listening)
        {
            // unbind
            if (keyCode == GLFW_KEY_ESCAPE || keyCode == GLFW_KEY_BACKSPACE)
            {
                ((MacroConfig) config).setValue(GLFW_KEY_UNKNOWN);
            }
            else
            {
                ((MacroConfig) config).setValue(keyCode);
            }
            listening = false;
        }
    }

    @Override
    public void charTyped(char character, int modifiers)
    {

    }

    public boolean isListening()
    {
        return listening;
    }

    public void setListening(boolean listening)
    {
        this.listening = listening;
    }
}
