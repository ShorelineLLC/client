package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.gui.clickgui.config.picker.ExpandableComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModuleComponent extends FrameComponent
{
    protected final Module module;
    private ConfigComponent<?> currentAnimation;

    protected final List<ConfigComponent<?>> components = new ArrayList<>();

    @Setter
    private boolean frameOpen;
    protected final Animation collapseAnim;

    public ModuleComponent(Module module,
                           Frame frame,
                           float x,
                           float y,
                           float frameWidth,
                           float frameHeight)
    {
        super(frame, x, y, frameWidth, frameHeight);
        this.module = module;

        float scale = ClickGuiModule.INSTANCE.getScale();
        for (Config<?> config : module.getConfigs())
        {
            if (config.getConfigGroup() != null)
            {
                continue;
            }

            final ComponentFactory factory = frame.getComponentFactory();
            ConfigComponent<?> component = factory.createConfigComponent(config, this, frame,
                    2.0f * scale,
                    0,
                    (frameWidth - 2.0f) * scale,
                    frameHeight * scale);

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
        boolean hovering = Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height);
        setHoverState(hovering);
        if (hovering)
        {
            ClickGuiScreen.INSTANCE.setDescriptionText(getModule().getDescription());
        }

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();
        float scale = ClickGuiModule.INSTANCE.getScale();

        int color = ColorUtil.brighten(theme.getComponentColor(), 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);
        drawText(context, module.getName(), getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());

        if (components.size() > 1)
        {
            String dotsText = "...";
            drawText(context, dotsText, getTx() + width - getTextWidth(dotsText) - 1.0f, getTy() + 4.0f, theme.getTextColor());
        }

        enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight());

        int pending = 0;
        float configY = scale * 2.0f;
        if (collapseAnim.getFactor() > 0.01f || frameOpen)
        {
            for (ConfigComponent<?> component : components)
            {
                if (currentAnimation == null)
                {
                    component.getDrawAnim().setState(component.getConfig().isVisible());
                    if (!component.getDrawAnim().isFinished())
                    {
                        currentAnimation = component;
                    }
                }
                else if (currentAnimation.getDrawAnim().isFinished())
                {
                    currentAnimation = null;
                }

                if (component.getDrawAnim().getFactor() > 0.01)
                {
                    float totalHeight = component.getHeight() + (float) Math.floor(scale);
                    if (component instanceof ExpandableComponent<?> c)
                    {
                        totalHeight += c.getComponentHeight();
                    }

                    totalHeight *= (float) component.getDrawAnim().getFactor();

                    enableScissor(context, component.getTx(), component.getTy(), component.getTx() + component.getWidth(), component.getTy() + totalHeight);
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

                if (!component.getDrawAnim().isFinished()
                        || component.getConfig().isVisible() && component.getDrawAnim().getFactor() != 1.0
                        || !component.getConfig().isVisible() && component.getDrawAnim().getFactor() != 0.0)
                {
                    pending++;
                }
            }
        } else
        {

            for (ConfigComponent<?> component : components)
            {
                component.reset();
            }
        }


        if (currentAnimation != null)
        {
            currentAnimation.getDrawAnim().setLength(100f / pending);
        }

        drawRect(context, getTx() + getWidth() - 1.0f, getTy() + getHeight(), 1.0f, (float) (configY * collapseAnim.getFactor()), color);
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

        if (frameOpen)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible() && component.getDrawAnim().getFactor() == 1.0f)
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
        if (frameOpen)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible() && component.getDrawAnim().getFactor() == 1.0f)
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
        if (frameOpen)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible() && component.getDrawAnim().getFactor() == 1.0f)
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
        if (frameOpen)
        {
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible() && component.getDrawAnim().getFactor() == 1.0f)
                {
                    component.charTyped(chr, modifiers);
                }
            }
        }
    }

    public float getComponentHeight()
    {
        float scaling = ClickGuiModule.INSTANCE.getScale();
        float frameHeight = scaling * 2;
        for (ConfigComponent<?> component : components)
        {
            if (component.getDrawAnim().getFactor() > 0.01)
            {
                float totalHeight = component.getHeight() + (float) Math.floor(scaling);
                if (component instanceof ExpandableComponent<?> c)
                {
                    totalHeight += c.getComponentHeight();
                }

                frameHeight += (float) (totalHeight * component.getDrawAnim().getFactor());
            }
        }

        return frameHeight;
    }

    public float getScaledHeight()
    {
        return (float) (getComponentHeight() * collapseAnim.getFactor());
    }
}
