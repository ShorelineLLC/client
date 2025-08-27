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
import net.shoreline.client.impl.module.client.ClickGuiModule;
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
                          float x,
                          float y,
                          float frameWidth,
                          float frameHeight)
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
        float scale = ClickGuiModule.INSTANCE.getScale();

        int color = ColorUtil.brighten(theme.getComponentColor(), 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        drawText(context, getConfig().getName(), getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());
        String dotsText = "...";
        drawText(context, dotsText, getTx() + width - getTextWidth(dotsText) - 1.0f, getTy() + 4.0f, theme.getTextColor());

        enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight());

        float configY = height + (3.0f * scale);
        for (ConfigComponent<?> component : components)
        {
            component.getDrawAnim().setState(component.getConfig().isVisible());
            if (component.getDrawAnim().getFactor() > 0.01)
            {
                float extra = 0.0f;
                if (component instanceof GroupComponent c)
                {
                    extra += c.getScaledHeight();
                }
                else if (component instanceof ColorPickerComponent c1)
                {
                    extra += c1.getComponentHeight();
                }

                enableScissor(context, component.getTx(), component.getTy(), component.getTx() + component.getWidth(), component.getTy() + component.getDrawHeight() + extra);
                component.setY(getYOffset());
                component.drawComponent(context, mouseX, mouseY, delta);
                component.setYOffset(configY);
                configY += component.getDrawHeight() + extra + (float) Math.floor(scale);

                component.setModuleOffset(configY);
                disableScissor(context);
            }
        }

        drawRect(context, getTx() + getWidth() - 1.0f, getTy() + getHeight(), 1.0f, configY, color);
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

    public float getComponentHeight()
    {
        float scale = ClickGuiModule.INSTANCE.getScale();
        float frameHeight = 2.0f * scale;
        for (ConfigComponent<?> component : components)
        {
            if (component.getConfig().isVisible())
            {
                if (component instanceof GroupComponent c)
                {
                    frameHeight += c.getScaledHeight();
                } else if (component instanceof ColorPickerComponent c1)
                {
                    frameHeight += c1.getComponentHeight();
                }

                frameHeight += component.getDrawHeight() + (float) Math.floor(scale);
            }
        }

        return frameHeight;
    }

    public float getScaledHeight()
    {
        float scale = ClickGuiModule.INSTANCE.getScale();
        return (float) ((getComponentHeight() + (2.0f * scale)) * collapseAnim.getFactor());
    }
}
