package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.Theme;
import net.shoreline.client.util.Keyboard;
import org.lwjgl.glfw.GLFW;

@Getter
public class KeyListenerComponent extends ConfigComponent<Macro>
{
    private boolean listening;

    public KeyListenerComponent(Config<Macro> config,
                                ModuleComponent moduleComponent,
                                Frame frame,
                                int x,
                                int y,
                                int frameWidth,
                                int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        String keyText = listening ? "..." : Keyboard.getKeyName(getConfig().getValue().getKeycode()).toUpperCase();
        drawText(context, getConfig().getName() + " " + Formatting.GRAY + keyText, getTx() + 3, getTy() + 4, theme.getTextColor());
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height))
        {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_1)
            {
                listening = !listening;
            }
            else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_2 && !listening)
            {
                // Reset the bind
                ((MacroConfig) getConfig()).setValue(GLFW.GLFW_KEY_UNKNOWN, null);
            }
            else
            {
                if (listening)
                {
                    // Ignore Right click
                    if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_2)
                    {
                        // Mouse bind
                        ((MacroConfig) getConfig()).setValue(
                                GLFW.GLFW_KEY_LAST + mouseButton, getConfig().getValue().getCommand());
                    }
                    listening = false;
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {

    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        if (listening)
        {
            // unbind
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE)
            {
                ((MacroConfig) getConfig()).setValue(GLFW.GLFW_KEY_UNKNOWN, null);
            }
            else
            {
                ((MacroConfig) getConfig()).setValue(keyCode, getConfig().getValue().getCommand());
            }
            listening = false;
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {

    }
}
