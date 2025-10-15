package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.util.text.Formatter;
import org.lwjgl.glfw.GLFW;

public class SelectorComponent extends ConfigComponent<Enum<?>>
{
    private int index;

    public SelectorComponent(Config<Enum<?>> config,
                             ModuleComponent moduleComponent,
                             Frame frame,
                             float x,
                             float y,
                             float frameWidth,
                             float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        index = ((EnumConfig<?>) config).getIndex();
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

        String selectorText = Formatter.formatEnum(getConfig().getValue());
        String formattedText = getConfig().getName() + " " + Formatting.GRAY + selectorText;
        drawText(context, formattedText, getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height))
        {
            Enum<?>[] values = ((EnumConfig<?>) getConfig()).getValues();
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                index = (index + 1) % values.length;
                getConfig().setValue(values[index]);
            }
            else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                index = (index - 1 + values.length) % values.length;
                getConfig().setValue(values[index]);
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
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
    }

    @Override
    protected void onConfigUpdate(Enum<?> value)
    {
        index = value.ordinal();
    }
}
