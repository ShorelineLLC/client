package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModuleComponent extends FrameComponent
{
    protected final Module module;

    private final List<ConfigComponent<?>> components = new ArrayList<>();

    @Setter
    private boolean collapsed;
    private final Animation collapseAnim = new Animation(false, 150L, Easing.CUBIC_IN_OUT);

    public ModuleComponent(Module module,
                           Frame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(frame, x, y, frameWidth, frameHeight);
        this.module = module;

        int configY = 2;
        for (Config<?> config : module.getConfigs())
        {
            final ComponentFactory factory = frame.getComponentFactory();
            ConfigComponent<?> component = factory.createConfigComponent(
                    config, frame, 2, configY, frameWidth - 2, frameHeight);

            components.add(component);
            configY += component.getHeight() + 1;
        }

        collapsed = true;
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        drawRect(context, getTx(), getTy(), width, height, theme.getComponentColor());
        drawText(context, module.getName(), getTx() + 3, getTy() + 4, theme.getTextColor());
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isInBounds(mouseX, mouseY, x, y, width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.collapsed = !collapsed;
            collapseAnim.setState(collapsed);
        }

        if (!isCollapsed())
        {
            for (ConfigComponent<?> component : components)
            {
                component.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
        if (!isCollapsed())
        {
            for (ConfigComponent<?> component : components)
            {
                component.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {
        if (!isCollapsed())
        {
            for (ConfigComponent<?> component : components)
            {
                component.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        if (!isCollapsed())
        {
            for (ConfigComponent<?> component : components)
            {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        if (!isCollapsed())
        {
            for (ConfigComponent<?> component : components)
            {
                component.charTyped(chr, modifiers);
            }
        }
    }
}
