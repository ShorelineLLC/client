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
            final ComponentFactory factory = frame.getComponentFactory();
            ConfigComponent<?> component = factory.createConfigComponent(
                    config, this, frame, 2, 0, frameWidth - 2, frameHeight);

            components.add(component);
        }

        this.collapseAnim = new Animation(false, 150L, Easing.CUBIC_IN_OUT);
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

        context.enableScissor(getTx(), getTy() + height, getTx() + width, getTy() + height + getComponentHeight());

        int configY = 2;
        for (ConfigComponent<?> component : components)
        {
            if (component.getConfig().isVisible())
            {
                component.drawComponent(context, mouseX, mouseY, delta);
                component.setYOffset(configY);
                configY += component.getHeight() + 1;
            }
        }

        context.disableScissor();
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
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {
        if (collapseAnim.getFactor() > 0.0)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
            if (component.getConfig().isVisible())
            {
                frameHeight += component.getHeight() + 1;
            }
        }
        return (int) (frameHeight * collapseAnim.getFactor());
    }
}
