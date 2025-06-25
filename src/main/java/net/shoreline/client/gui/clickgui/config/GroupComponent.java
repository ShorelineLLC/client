package net.shoreline.client.gui.clickgui.config;

import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.font.GlyphBuffer;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.*;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GroupComponent extends ConfigComponent<Void>
{
    @Setter
    private boolean groupOpen;
    private final Animation collapseAnim;

    private final List<ConfigComponent<?>> components = new ArrayList<>();

    private final GlyphBuffer dotsBuffer = new GlyphBuffer();

    public GroupComponent(Config<Void> config,
                          ModuleComponent moduleComponent,
                          Frame frame,
                          int x,
                          int y,
                          int frameWidth,
                          int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        ConfigGroup group = (ConfigGroup) config;
        for (Config<?> config1 : group)
        {
            final ComponentFactory factory = frame.getComponentFactory();
            ConfigComponent<?> component = factory.createConfigComponent(
                    config1, moduleComponent, frame, 4, getTy(), frameWidth - 2, frameHeight);

            components.add(component);
            frame.getAllComponents().add(component);
        }

        this.collapseAnim = new Animation(false, 150L, Easing.CUBIC_IN_OUT);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height));
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.brighten(theme.getComponentColor(), 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        drawText(context, textBuffer, Text.literal(getConfig().getName()).withColor(theme.getTextColor()), getTx() + 3, getTy() + 4);
        Text dotsText = Text.literal("...").withColor(theme.getTextColor());
        drawText(context, dotsBuffer, dotsText, getTx() + width - getTextWidth(dotsBuffer, dotsText) - 1, getTy() + 4, 0.0f, 5.0f);

        enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight());

        int configY = 2;
        for (ConfigComponent<?> component : components)
        {
            if (component.getConfig().isVisible())
            {
                component.drawComponent(context, mouseX, mouseY, delta);
                component.setYOffset(configY - getComponentHeight());
                configY += component.getHeight() + 1;
            }
        }

        disableScissor(context);
    }

    @Override
    public int getTy()
    {
        return super.getTy();
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.groupOpen = !groupOpen;
            collapseAnim.setState(groupOpen);
        }

        if (collapseAnim.getFactor() > 0.0)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.charTyped(chr, modifiers);
                }
            }
        }
    }

    public int getComponentHeight()
    {
        int frameHeight = 0;
        for (ConfigComponent<?> component : components)
        {
            if (component.getConfig().isVisible())
            {
                frameHeight += component.getHeight() + 1;
            }
        }
        return frameHeight;
    }

    public int getScaledHeight()
    {
        return (int) ((getComponentHeight() + 2) * collapseAnim.getFactor());
    }
}
