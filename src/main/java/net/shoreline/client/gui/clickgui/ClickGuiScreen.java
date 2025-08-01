package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.gui.clickgui.config.KeyListenerComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen
{
    public static ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private final List<Frame> guiFrames = new ArrayList<>();

    @Getter
    private final Mouse mouse = new Mouse();
    private boolean draggingMouse;

    private boolean shouldCloseOnEsc = true;

    protected ClickGuiScreen()
    {
        super(Text.of("Shoreline-ClickGui"));

        int frameOffset = 15;
        for (GuiCategory category : GuiCategory.values())
        {
            if (category.equals(GuiCategory.HUD))
            {
                continue;
            }
            Frame frame = new GuiCategoryFrame(category, frameOffset, 15, 120, 17);
            guiFrames.add(frame);
            frameOffset += frame.getWidth() + 4;
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
        if (ClickGuiModule.INSTANCE.shouldDarken())
        {
            Animation animation = ClickGuiModule.INSTANCE.getFadeAnimation();
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

        for (Frame frame : guiFrames)
        {
            if (!draggingMouse && mouse.isHovering(frame.getX(), frame.getY(), frame.getWidth(), frame.getTitleHeight()) && mouse.isLeftHeld())
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
            int scrolledY = (int) (verticalAmount * ClickGuiModule.INSTANCE.getScrollSpeed());
            int y = frame.getY() + scrolledY;
            int minY = -frame.getComponentHeight();
            frame.setY(MathHelper.clamp(y, minY, scrolledY > 0 ? 15 : Integer.MAX_VALUE));
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode,
                              int scanCode,
                              int modifiers)
    {
        shouldCloseOnEsc = true;
        for (Frame frame : guiFrames)
        {
            for (FrameComponent component : frame.getAllComponents())
            {
                if (component instanceof KeyListenerComponent keyListener && keyListener.isListening()
                        || component instanceof TextComponent text && text.isTyping())
                {
                    shouldCloseOnEsc = false;
                    break;
                }
            }

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

    @Override
    public boolean shouldCloseOnEsc()
    {
        return shouldCloseOnEsc;
    }

    @Override
    public void close()
    {
        for (Frame frame : guiFrames)
        {
            frame.setDragging(false);
        }
        draggingMouse = false;
        mouse.setLeftClicked(false);
        mouse.setRightClicked(false);
        mouse.setLeftHeld(false);
        mouse.setRightHeld(false);
        ClickGuiModule.INSTANCE.disable();
        super.close();
    }

    public Theme getTheme()
    {
        return ClickGuiModule.INSTANCE.getTheme();
    }
}
