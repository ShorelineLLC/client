package net.shoreline.client.gui.hud;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.impl.HudModule;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.HudGuiModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HudGuiScreen extends Screen
{
    public static HudGuiScreen INSTANCE = new HudGuiScreen();

    @Getter
    private final Mouse mouse = new Mouse();
    private boolean draggingMouse;

    private final HudFrame hudFrame;
    private final List<HudComponent> hudComponents = new ArrayList<>();

    protected HudGuiScreen()
    {
        super(Text.of("Shoreline-Hud"));

        hudFrame = new HudFrame("HUD", 100, 50, 120, 17);
        for (HudModule module : Managers.MODULES.getHudModules())
        {
            HudComponent component = new HudComponent(module, module.getX(), module.getY());
            hudComponents.add(component);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks)
    {
        if (client.world == null)
        {
            renderPanoramaBackground(context, deltaTicks);
        }
    }

    @Override
    public void render(DrawContext context,
                       int mouseX,
                       int mouseY,
                       float deltaTicks)
    {
        Animation animation = ClickGuiModule.INSTANCE.getFadeAnimation();
        if (ClickGuiModule.INSTANCE.shouldDarken())
        {
            int backgroundColor = ColorUtil.withTransparency(0x66000000, (float) animation.getFactor());
            context.fill(
                    0,
                    0,
                    context.getScaledWindowWidth(),
                    context.getScaledWindowHeight(),
                    backgroundColor
            );
        }

        if (ClickGuiModule.INSTANCE.shouldBlur())
        {
            applyBlur();
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int lineColor = ColorUtil.withTransparency(0x50ffffff, (float) animation.getFactor());
        int x = screenWidth / 2 - 1;
        int y = screenHeight / 2 - 1;
        context.fill(x, 0, x + 2, screenHeight, lineColor);
        context.fill(0, y, screenWidth, y + 2, lineColor);

        hudFrame.drawComponent(context, mouseX, mouseY, deltaTicks);

        for (HudComponent component : hudComponents)
        {
            if (!draggingMouse && mouse.isHovering(component.getX(), component.getY(), component.getWidth(), component.getHeight()) && mouse.isLeftHeld())
            {
                component.setDragging(true);
                draggingMouse = true;
            }

            if (component.isDragging())
            {
                clampOverlap(component);
            }

            HudModule module = component.getHudModule();
            if (module.isEnabled())
            {
                component.drawComponent(context, mouseX, mouseY, deltaTicks);
            }
        }

        mouse.setLeftClicked(false);
        mouse.setRightClicked(false);
        mouse.setMouseX(mouseX);
        mouse.setMouseY(mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX,
                                double mouseY,
                                int mouseButton)
    {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            mouse.setLeftClicked(true);
            mouse.setLeftHeld(true);
        } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            mouse.setRightClicked(true);
            mouse.setRightHeld(true);
        }

        hudFrame.mouseClicked(mouseX, mouseY, mouseButton);

        for (HudComponent component : hudComponents)
        {
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            mouse.setLeftHeld(false);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            mouse.setRightHeld(false);
        }

        hudFrame.mouseReleased(mouseX, mouseY, button);

        for (HudComponent component : hudComponents)
        {
            if (component.isDragging())
            {
                clampComponents(component);
            }

            component.mouseReleased(mouseX, mouseY, button);
        }

        draggingMouse = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause()
    {
        return false;
    }

    @Override
    public void close()
    {
        hudFrame.setDragging(false);
        for (HudComponent component : hudComponents)
        {
            component.setDragging(false);
        }
        draggingMouse = false;
        mouse.setLeftClicked(false);
        mouse.setRightClicked(false);
        mouse.setLeftHeld(false);
        mouse.setRightHeld(false);
        HudGuiModule.INSTANCE.disable();
        super.close();
    }

    private void clampOverlap(HudComponent component)
    {
        for (HudComponent c : hudComponents)
        {
            if (c.equals(component))
            {
                continue;
            }

            if (component.getX() < c.getX() + c.getWidth() &&
                    component.getX() + component.getWidth() > c.getX() &&
                    component.getY() < c.getY() + c.getHeight() &&
                    component.getY() + component.getHeight() > c.getY())
            {
                if (component.getX() < c.getX())
                {
                    component.setX(c.getX() - component.getWidth());
                } else
                {
                    component.setX(c.getX() + c.getWidth());
                }

                if (component.getY() < c.getY())
                {
                    component.setY(c.getY() + component.getHeight());
                } else
                {
                    component.setY(c.getY() - c.getHeight());
                }
            }
        }
    }

    private void clampComponents(HudComponent component)
    {
        int snapThreshold = 8;
        for (HudComponent c : hudComponents)
        {
            if (c.equals(component))
            {
                continue;
            }

            int cx = c.getX();
            int cy = c.getY();
            int cw = c.getWidth();
            int ch = c.getHeight();

            if (Math.abs(component.getX() + component.getWidth() - cx) < snapThreshold)
            {
                component.setX(cx - component.getWidth());
            } else if (Math.abs(component.getX() - (cx + cw)) < snapThreshold)
            {
                component.setX(cx + cw);
            } else if (Math.abs(component.getX() - cx) < snapThreshold)
            {
                component.setX(cx);
            } else if (Math.abs(component.getX() + component.getWidth() - (cx + cw)) < snapThreshold)
            {
                component.setX(cx + cw - component.getWidth());
            }

            if (Math.abs(component.getY() + component.getHeight() - cy) < snapThreshold)
            {
                component.setY(cy - component.getHeight());
            } else if (Math.abs(component.getY() - (cy + ch)) < snapThreshold)
            {
                component.setY(cy + ch);
            } else if (Math.abs(component.getY() - cy) < snapThreshold)
            {
                component.setY(cy);
            } else if (Math.abs(component.getY() + component.getHeight() - (cy + ch)) < snapThreshold)
            {
                component.setY(cy + ch - component.getHeight());
            }
        }
    }
}
