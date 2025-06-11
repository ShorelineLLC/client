package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.gui.ScissorStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen
{
    public static final ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private final List<Frame> guiFrames = new ArrayList<>();

    @Getter
    private final Mouse mouse = new Mouse();

    @Getter
    private final ScissorStack scissorStack = new ScissorStack();

    @Getter
    private final Theme theme = new ThemeBuilder()
            .setTitleColor(0xff000000)
            .setBackgroundColor(0xff000000)
            .setOutlineColor(0xff000000)
            .setModuleColor(0xff000000)
            .setSettingColor(0xff000000)
            .build();

    protected ClickGuiScreen()
    {
        super(Text.of("Shoreline-ClickGui"));

        int frameOffset = 15;
        for (GuiCategory category : GuiCategory.values())
        {
            Frame frame = new Frame(category.getName(), frameOffset, 15, 105, 15);
            guiFrames.add(frame);
            frameOffset += frame.getWidth() + 2;
        }
    }

    @Override
    public void render(DrawContext context,
                       int mouseX,
                       int mouseY,
                       float deltaTicks)
    {
        for (Frame frame : guiFrames)
        {
            if (mouse.isInBounds(frame.getX(), frame.getY(), frame.getWidth(), frame.getTitleHeight()) && mouse.isLeftHeld())
            {
                frame.setDragging(true);
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
            frame.mouseReleased(mouseX, mouseY, button);
        }

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
