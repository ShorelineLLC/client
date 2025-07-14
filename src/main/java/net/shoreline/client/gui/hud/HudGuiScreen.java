package net.shoreline.client.gui.hud;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.shoreline.client.api.module.HudModule;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.HudGuiModule;
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
        applyBlur(context);
        renderDarkening(context);

        if (!draggingMouse && mouse.isHovering(hudFrame.getX(), hudFrame.getY(), hudFrame.getWidth(), hudFrame.getTitleHeight()) && mouse.isLeftHeld())
        {
            hudFrame.setDragging(true);
            draggingMouse = true;
        }

        hudFrame.drawComponent(context, mouseX, mouseY, deltaTicks);

        for (HudComponent component : hudComponents)
        {
            if (!draggingMouse && mouse.isHovering(component.getX(), component.getY(), component.getWidth(), component.getHeight()) && mouse.isLeftHeld())
            {
                component.setDragging(true);
                draggingMouse = true;
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
            component.setDragging(false);
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
}
