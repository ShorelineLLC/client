package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.gui.Mouse;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen
{
    public static final ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private final List<Frame> guiFrames = new ArrayList<>();

    @Getter
    private final Mouse mouse = new Mouse();
    private boolean draggingMouse;

    @Getter
    private final Theme theme = new ThemeBuilder()
            .setTitleColor(0xcc3500a4)
            .setBackgroundColor(0xb32e0094)
            .setOutlineColor(0xb37a3cf2)
            .setComponentColor(0xcc3500a4)
            .setTextColor(0xffffffff)
            .build();

    protected ClickGuiScreen()
    {
        super(Text.of("Shoreline-ClickGui"));

        int frameOffset = 15;
        for (GuiCategory category : GuiCategory.values())
        {
            Frame frame = new GuiCategoryFrame(category, frameOffset, 15, 120, 17);
            guiFrames.add(frame);
            frameOffset += frame.getWidth() + 4;
        }
    }

    @Override
    public void render(DrawContext context,
                       int mouseX,
                       int mouseY,
                       float deltaTicks)
    {
        applyBlur();

        for (Frame frame : guiFrames)
        {
            if (!draggingMouse && mouse.isInBounds(frame.getX(), frame.getY(), frame.getWidth(), frame.getTitleHeight()) && mouse.isLeftHeld())
            {
                frame.setDragging(true);
                draggingMouse = true;
            }

            frame.drawComponent(context, mouseX, mouseY, deltaTicks);
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

        for (Frame frame : guiFrames)
        {
            frame.mouseClicked(mouseX, mouseY, mouseButton);
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

        for (Frame frame : guiFrames)
        {
            frame.setDragging(false);
            frame.mouseReleased(mouseX, mouseY, button);
        }

        draggingMouse = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX,
                                 double mouseY,
                                 double horizontalAmount,
                                 double verticalAmount)
    {
        for (Frame frame : guiFrames)
        {
            frame.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode,
                              int scanCode,
                              int modifiers)
    {
        for (Frame frame : guiFrames)
        {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr,
                             int modifiers)
    {
        for (Frame frame : guiFrames)
        {
            frame.charTyped(chr, modifiers);
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause()
    {
        return false;
    }
}
