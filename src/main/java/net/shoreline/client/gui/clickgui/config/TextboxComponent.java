package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

public class TextboxComponent extends ConfigComponent<String>
{
    private final TextComponent textComponent;

    public TextboxComponent(Config<String> config,
                            ModuleComponent moduleComponent,
                            Frame frame,
                            float x,
                            float y,
                            float frameWidth,
                            float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        textComponent = new TextComponent(frame, x, y, frameWidth, frameHeight,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                config::getValue, config::setValue);

        frame.getAllComponents().add(textComponent);
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

        textComponent.setYOffset(getYOffset());
        textComponent.setX(getTx());
        textComponent.setY(getTy());

        if (textComponent.isTyping())
        {
            textComponent.drawComponent(context, mouseX, mouseY, delta);
        } else
        {
            int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
            drawRect(context, getTx(), getTy(), width, height, color);

            String formattedText = getConfig().getName() + " " + Formatting.GRAY + getConfig().getValue();
            drawText(context, formattedText, getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());
        }
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        textComponent.mouseClicked(mouseX, mouseY, mouseButton);
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
        textComponent.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        textComponent.charTyped(chr, modifiers);
    }

    @Override
    protected void onConfigUpdate(String value)
    {
        textComponent.updateBuffer(value);
    }
}
