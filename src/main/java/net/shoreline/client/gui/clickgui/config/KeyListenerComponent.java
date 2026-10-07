package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.HoldKeybind;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.util.Keyboard;
import org.lwjgl.glfw.GLFW;

@Getter
public class KeyListenerComponent extends ConfigComponent<Macro>
{
    private boolean listening;

    public KeyListenerComponent(Config<Macro> config,
                                ModuleComponent moduleComponent,
                                Frame frame,
                                float x,
                                float y,
                                float frameWidth,
                                float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        boolean hovering = Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height);
        setHoverState(hovering);
        if (hovering)
        {
            ClickGuiScreen.INSTANCE.setDescriptionText(getConfig().getDescription());
        }

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        String keyText = listening ? "..." : Keyboard.getKeyName(getConfig().getValue().getKeycode()).toUpperCase();
        String formattedText = getConfig().getName() + " " + Formatting.GRAY + keyText;
        drawText(context, formattedText, getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());
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
                int keyCode = config.getValue().getKeycode();
                boolean hold = config.getValue() instanceof HoldKeybind;
                if (hold)
                {
                    config.setValue(new ModuleKeybind(keyCode, (Toggleable) getModuleComponent().getModule()));
                }
                else
                {
                    config.setValue(new HoldKeybind(keyCode, (Toggleable) getModuleComponent().getModule()));
                }

                ClickGuiScreen.INSTANCE.addNotification(String.format("Bind set to %s", !hold ? "Hold" : "Toggle"), 1000);
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
