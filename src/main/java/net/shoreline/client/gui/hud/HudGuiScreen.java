package net.shoreline.client.gui.hud;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.shoreline.client.Shoreline;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.gui.clickgui.config.KeyListenerComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.HudGuiModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
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
            HudComponent component = new HudComponent(module, (int) module.getX(), (int) module.getY());
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

        if (mouse.isHovering(hudFrame.getX(), hudFrame.getY(), hudFrame.getWidth(), hudFrame.getTitleHeight()) && mouse.isLeftHeld())
        {
            if (!draggingMouse)
            {
                hudFrame.setDragging(true);
                draggingMouse = true;
            }
        }

        hudFrame.drawComponent(context, mouseX, mouseY, deltaTicks);

        runAnchorTick(false);
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

        hudFrame.setDragging(false);
        hudFrame.mouseReleased(mouseX, mouseY, button);

        for (HudComponent component : hudComponents)
        {
            component.mouseReleased(mouseX, mouseY, button);
        }

        draggingMouse = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode,
                              int scanCode,
                              int modifiers)
    {
        hudFrame.keyPressed(keyCode, scanCode, modifiers);
        hudComponents.forEach(hudComponent -> hudComponent.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr,
                             int modifiers)
    {
        hudFrame.charTyped(chr, modifiers);
        hudComponents.forEach(hudComponent -> hudComponent.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
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

    public void runAnchorTick(boolean modifyComponent)
    {
        for (Anchor anchor : Anchor.values())
        {
            if (anchor == Anchor.None)
            {
                continue;
            }

            float offset = 2f;
            List<HudComponent> anchoredElements = hudComponents.stream()
                    .filter(e -> e.getHudModule().getAnchor() == anchor)
                    .sorted(Comparator.comparingInt(e -> e.getHudModule().getIndex()))
                    .toList();

            if (anchoredElements.isEmpty())
            {
                continue;
            }

            int i = 0;
            float currentY = anchor.getY(MinecraftClient.getInstance().getWindow().getScaledHeight(), 0, offset);
            for (HudComponent hudModule : anchoredElements)
            {
                if (hudModule.isDragging())
                {
                    continue;
                }

                hudModule.getHudModule().setIndex(i);
                i++;

                if (!hudModule.getHudModule().isEnabled())
                {
                    continue;
                }

                hudModule.setX(modifyComponent, anchor.getX(MinecraftClient.getInstance().getWindow().getScaledWidth(), hudModule.getHudModule().getWidth()));
                switch (anchor)
                {
                    case Top_Left:
                    case Top_Right:
                        hudModule.setY(modifyComponent, currentY);
                        currentY += hudModule.getHudModule().getHeight();
                        break;
                    case Bottom_Left:
                    case Bottom_Right:
                        hudModule.setY(modifyComponent, currentY -= hudModule.getHudModule().getHeight());
                        break;
                }
            }
        }
    }
}
