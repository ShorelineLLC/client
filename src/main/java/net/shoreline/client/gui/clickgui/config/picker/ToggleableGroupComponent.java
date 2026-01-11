package net.shoreline.client.gui.clickgui.config.picker;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.impl.render.animation.Animation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ToggleableGroupComponent extends ExpandableComponent<Boolean>
{
    @Getter
    private final List<ConfigComponent<?>> components = new ArrayList<>();
    private final Animation toggleAnim;

    public ToggleableGroupComponent(Config<Boolean> config,
                                    ModuleComponent moduleComponent,
                                    Frame frame,
                                    float x,
                                    float y,
                                    float frameWidth,
                                    float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.toggleAnim = new Animation(config.getValue(), 150);
    }

    @Override
    public void drawComponent(DrawContext context, float mouseX, float mouseY, float delta)
    {
        boolean hovering = Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height);
        setHoverState(hovering);

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();
        float scale = ClickGuiModule.INSTANCE.getScale();

        int color = ColorUtil.withTransparency(theme.getComponentColor(), (float) toggleAnim.getFactor());
        color = ColorUtil.brighten(toggleAnim.getFactor() > 0.0 ? color : 0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);

        drawText(context, getConfig().getName(), getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());
        String dotsText = "...";
        drawText(context, dotsText, getTx() + width - getTextWidth(dotsText) - 1.0f, getTy() + 4.0f, theme.getTextColor());

        enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getComponentHeight());

        float configY = height + (3.0f * scale);
        if (collapseAnim.getFactor() > 0.01f)
        {
            for (ConfigComponent<?> component : components)
            {
                component.getDrawAnim().setState(component.getConfig().isVisible());
                if (component.getDrawAnim().getFactor() > 0.01)
                {
                    float totalHeight = component.getHeight() + (float) Math.floor(scale);
                    if (component instanceof ExpandableComponent<?> c)
                    {
                        totalHeight += c.getComponentHeight();
                    }

                    totalHeight *= (float) component.getDrawAnim().getFactor();

                    enableScissor(context, component.getTx(), component.getTy(), component.getTx() + component.getWidth(), component.getTy() + totalHeight);
                    component.setY(getYOffset());
                    component.drawComponent(context, mouseX, mouseY, delta);
                    component.setYOffset(configY);
                    configY += totalHeight;

                    component.setModuleOffset(configY);
                    disableScissor(context);
                }
                else
                {
                    component.reset();
                }
            }

        } else
        {

            for (ConfigComponent<?> component : components)
            {
                component.reset();
            }
        }

        drawRect(context, getTx() + getWidth() - 1.0f, getTy() + getHeight(), 1.0f, configY, color);
        disableScissor(context);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height))
        {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.pickerOpen = !pickerOpen;
                collapseAnim.setState(pickerOpen);
                collapseAnim.setEasing(pickerOpen ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
            }
            else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                getConfig().setValue(!getConfig().getValue());
                toggleAnim.setState(getConfig().getValue());
            }
        }

        if (pickerOpen)
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
    public void mouseReleased(double mouseX, double mouseY, int button)
    {
        if (pickerOpen)
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
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (pickerOpen)
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
    public void charTyped(char chr, int modifiers)
    {
        if (pickerOpen)
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

    @Override
    public float getComponentHeight()
    {
        float scale = ClickGuiModule.INSTANCE.getScale();
        float frameHeight = 4.0f * scale;
        for (ConfigComponent<?> component : components)
        {
            if (component.getDrawAnim().getFactor() > 0.01)
            {
                float totalHeight = component.getHeight() + (float) Math.floor(scale);
                if (component instanceof ExpandableComponent<?> c)
                {
                    totalHeight += c.getComponentHeight();
                }

                frameHeight += (float) (totalHeight * component.getDrawAnim().getFactor());
            }
        }

        return (float) (frameHeight * collapseAnim.getFactor());
    }
}
