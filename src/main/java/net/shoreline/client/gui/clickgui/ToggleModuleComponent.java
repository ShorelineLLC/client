package net.shoreline.client.gui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.components.ToggleComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.gui.clickgui.config.picker.ExpandableComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;

public class ToggleModuleComponent extends ModuleComponent
{
    private final ToggleComponent toggleComponent;
    private ConfigComponent<?> currentAnimation;

    public ToggleModuleComponent(Toggleable module,
                                 Frame frame,
                                 float x,
                                 float y,
                                 float frameWidth,
                                 float frameHeight)
    {
        super(module, frame, x, y, frameWidth, frameHeight);
        this.toggleComponent = new ToggleComponent(frame, x, y, frameWidth, frameHeight, module.isEnabled(), module::toggle);

        module.getEnabled().addObserver(this::onModuleToggled);
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

        toggleComponent.setYOffset(getYOffset());
        toggleComponent.setX(getTx());
        toggleComponent.setY(getTy());
        toggleComponent.drawComponent(context, mouseX, mouseY, delta);

        int textColor = ColorUtil.interpolateColor(1.0f - (float) toggleComponent.getFactor(), theme.getColor(0xFFAAAAAA, 1.0f), theme.getTextColor());
        drawText(context, module.getName(), getTx() + 3.0f, getTy() + 4.0f, textColor);

        if (components.size() > 1)
        {
            String dotsText = "...";
            drawText(context, dotsText, getTx() + width - getTextWidth(dotsText) - 1.0f, getTy() + 4.0f, textColor);
        }

        if (getCollapseAnim().getFactor() > 0.0)
        {
            enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight() + 1);

            int pending = 0;
            float configY = 2.0f * scale;
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
                    component.setYOffset(configY);
                    component.drawComponent(context, mouseX, mouseY, delta);
                    configY += totalHeight;

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

                if (collapseAnim.getFactor() < 0.01f)
                {
                    component.reset();
                }
            }

            if (currentAnimation != null)
            {
                currentAnimation.getDrawAnim().setLength(100f / pending);
            }

            int color = ColorUtil.brighten(theme.getComponentColor(), 70, (float) toggleComponent.getHoverAnim().getFactor());
            drawRect(context, getTx() + getWidth() - 1.0f, getTy() + getHeight() + 0.1f, 1.0f, (float) (configY * collapseAnim.getFactor()), color);
            disableScissor(context);
        }
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        toggleComponent.mouseClicked(mouseX, mouseY, mouseButton);
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height) && mouseButton == 2)
        {
            Toggleable toggleable = (Toggleable) module;
            toggleable.setHidden(!toggleable.isHidden());
            String notification = toggleable.getName() + (toggleable.isHidden()
                            ? " is now hidden"
                            : " is now visible");
            ClickGuiScreen.INSTANCE.addNotification(notification, 1000);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void onModuleToggled(boolean enabled)
    {
        toggleComponent.setState(enabled);
    }
}
