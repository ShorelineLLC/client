package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.gui.clickgui.config.ColorPickerComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.gui.clickgui.config.GroupComponent;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ModuleComponent extends FrameComponent
{
    protected final Module module;

    protected final List<ConfigComponent<?>> components = new ArrayList<>();

    @Setter
    private boolean frameOpen;
    private final Animation collapseAnim;

    public ModuleComponent(Module module,
                           Frame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(frame, x, y, frameWidth, frameHeight);
        this.module = module;

        for (Config<?> config : module.getConfigs())
        {
            if (config.getConfigGroup() != null)
            {
                continue;
            }

            final ComponentFactory factory = frame.getComponentFactory();
            ConfigComponent<?> component = factory.createConfigComponent(
                    config, this, frame, 2, 0, frameWidth - 2, frameHeight);

            components.add(component);
            frame.getAllComponents().add(component);
        }

        this.collapseAnim = new Animation(false, 200, Easing.CUBIC_IN_OUT);
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
        drawText(context, module.getName(), getTx() + 3, getTy() + 4, theme.getTextColor());

        if (components.size() > 1)
        {
            String dotsText = "...";
            drawText(context, dotsText, getTx() + width - getTextWidth(dotsText) - 1, getTy() + 4, theme.getTextColor());
        }

        enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight());

        int configY = 2;
        for (ConfigComponent<?> component : components)
        {
            if (component.getDrawAnim().getFactor() > 0.01)
            {
                int extra = 0;
                if (component instanceof GroupComponent c)
                {
                    extra += c.getScaledHeight();
                }
                else if (component instanceof ColorPickerComponent c1)
                {
                    extra += c1.getComponentHeight();
                }

                context.enableScissor(component.getTx(), component.getTy(), component.getTx() + component.getWidth(), component.getTy() + component.getDrawHeight() + extra);
                component.getDrawAnim().setState(component.getConfig().isVisible());
                component.drawComponent(context, mouseX, mouseY, delta);
                component.setYOffset(configY);
                configY += component.getDrawHeight() + extra + 1;

                component.setModuleOffset(configY);
                context.disableScissor();
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
            this.frameOpen = !frameOpen;
            collapseAnim.setState(frameOpen);
            collapseAnim.setEasing(frameOpen ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
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
        int frameHeight = 2;
        for (ConfigComponent<?> component : components)
        {
            if (component.getDrawAnim().getFactor() > 0.01)
            {
                if (component instanceof GroupComponent c)
                {
                    frameHeight += c.getScaledHeight();
                } else if (component instanceof ColorPickerComponent c1)
                {
                    frameHeight += c1.getComponentHeight();
                }

                frameHeight += component.getDrawHeight() + 1;
            }
        }
        return frameHeight;
    }

    public int getScaledHeight()
    {
        return (int) (getComponentHeight() * collapseAnim.getFactor());
    }
}
