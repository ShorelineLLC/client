package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.api.module.Toggleable;
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
        Text formattedText = Text.empty()
                .append(Text.literal(getConfig().getName()).withColor(theme.getTextColor()))
                .append(Text.literal(" " + keyText).formatted(Formatting.GRAY));
        drawText(context, formattedText, getTx() + 3, getTy() + 4);
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height))
        {
            MacroConfig config = (MacroConfig) getConfig();
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_1)
            {
                listening = !listening;
            }
            else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_2 && !listening)
            {
                // Reset the bind
                config.setValue(new ModuleKeybind(GLFW.GLFW_KEY_UNKNOWN,
                        (Toggleable) getModuleComponent().getModule()));
            }
            else
            {
                if (listening)
                {
                    // Ignore Right click
                    if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_2)
                    {
                        // Mouse bind
                        config.setValue(new ModuleKeybind(GLFW.GLFW_KEY_LAST + mouseButton,
                                (Toggleable) getModuleComponent().getModule()));
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
            MacroConfig config = (MacroConfig) getConfig();
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE)
            {
                config.setValue(new ModuleKeybind(GLFW.GLFW_KEY_UNKNOWN, (Toggleable) getModuleComponent().getModule()));
            }
            else
            {
                config.setValue(new ModuleKeybind(keyCode, (Toggleable) getModuleComponent().getModule()));
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
