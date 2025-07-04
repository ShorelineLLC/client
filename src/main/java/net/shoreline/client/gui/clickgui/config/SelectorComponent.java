package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.Theme;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.util.Formatter;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class SelectorComponent extends ConfigComponent<Enum<?>>
{
    private int index;

    public SelectorComponent(Config<Enum<?>> config,
                             ModuleComponent moduleComponent,
                             Frame frame,
                             int x,
                             int y,
                             int frameWidth,
                             int frameHeight)
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
        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height));
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        String selectorText = Formatter.formatEnum(getConfig().getValue());
        Text formattedText = Text.empty()
                .append(Text.literal(getConfig().getName()).withColor(theme.getTextColor()))
                .append(Text.literal(" " + selectorText).formatted(Formatting.GRAY));
        drawText(context, textBuffer, formattedText, getTx() + 3, getTy() + 4);
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height))
        {
            Enum<?> val = getConfig().getValue();
            String[] values = Arrays.stream(val.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                index = index + 1 > values.length - 1 ? 0 : index + 1;
                getConfig().setValue(Enum.valueOf(val.getClass(), values[index]));
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                index = index - 1 < 0 ? values.length - 1 : index - 1;
                getConfig().setValue(Enum.valueOf(val.getClass(), values[index]));
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
