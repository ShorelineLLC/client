package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.impl.render.Theme;
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

    @Getter
    private final List<ConfigComponent<?>> components = new ArrayList<>();

    public GroupComponent(Config<Void> config,
                          ModuleComponent moduleComponent,
                          Frame frame,
                          int x,
                          int y,
                          int frameWidth,
                          int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
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

        drawText(context, getConfig().getName(), getTx() + 3, getTy() + 4, theme.getTextColor());
        String dotsText = "...";
        drawText(context, dotsText, getTx() + width - getTextWidth(dotsText) - 1, getTy() + 4, theme.getTextColor());

        enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight());

        int configY = height + 3;
        for (ConfigComponent<?> component : components)
        {
            if (component.getConfig().isVisible())
            {
                component.setY(getYOffset());
                component.drawComponent(context, mouseX, mouseY, delta);
                component.setYOffset(configY);
                configY += component.getHeight() + 1;
                if (component instanceof GroupComponent c)
                {
                    configY += c.getScaledHeight();
                } else if (component instanceof ColorPickerComponent c1)
                {
                    configY += c1.getComponentHeight();
                }

                component.setModuleOffset(configY);
            }
        }

        disableScissor(context);
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
            collapseAnim.setEasing(groupOpen ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
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
                frameHeight += component.getDrawHeight() + 1;
                if (component instanceof GroupComponent c)
                {
                    frameHeight += c.getScaledHeight();
                } else if (component instanceof ColorPickerComponent c1)
                {
                    frameHeight += c1.getComponentHeight();
                }
            }
        }
        return frameHeight;
    }

    public int getScaledHeight()
    {
        return (int) ((getComponentHeight() + 2) * collapseAnim.getFactor());
    }
}
