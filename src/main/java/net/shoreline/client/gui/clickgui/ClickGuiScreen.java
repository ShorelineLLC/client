package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.components.FrameComponent;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.gui.clickgui.config.KeyListenerComponent;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.impl.render.animation.Animation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ClickGuiScreen extends Screen
{
    public static ClickGuiScreen INSTANCE = new ClickGuiScreen();

    private final List<Frame> guiFrames = new ArrayList<>();
    private final Queue<GuiNotification> notifications = new ArrayDeque<>();

    @Getter
    private final Mouse mouse = new Mouse();
    private boolean draggingMouse;

    private boolean shouldCloseOnEsc = true;

    private String descriptionText;
    private boolean updateDescText;
    private final Animation descAnimation = new Animation(300L);

    protected ClickGuiScreen()
    {
        super(Text.of("Shoreline-ClickGui"));

        float frameOffset = 15.0f;
        for (GuiCategory category : GuiCategory.values())
        {
            if (category.equals(GuiCategory.HUD))
            {
                continue;
            }
            Frame frame = new GuiCategoryFrame(category, frameOffset, 15, 120, 17);
            guiFrames.add(frame);
            frameOffset += frame.getWidth() + 4.0f;
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
        updateDescText = false;
        float scale = ClickGuiModule.INSTANCE.getScale();
        final int scaledMx = (int) (mouseX / scale);
        final int scaledMy = (int) (mouseY / scale);

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

        if (client.world != null && ClickGuiModule.INSTANCE.shouldBlur())
        {
            applyBlur();
        }

        if (descriptionText != null)
        {
            float width = Managers.RENDER.getTextWidth(descriptionText);
            Managers.RENDER.drawText(context.getMatrices(),  descriptionText, context.getScaledWindowWidth() - width - 2,
                                     context.getScaledWindowHeight() - 16,
                                     ColorUtil.withTransparency(Colors.WHITE, (float) descAnimation.getFactor()));
        }

        GuiNotification notification = notifications.peek();
        if (notification != null)
        {
            if (notification.isExpired())
            {
                notifications.remove(notification);
            }
            else
            {
                notification.render(context);
            }
        }

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();

        matrixStack.scale(scale, scale, 1.0f);

        for (Frame frame : guiFrames)
        {
            if (mouse.isHovering(frame.getX(), frame.getY(), frame.getWidth(), frame.getTitleHeight()))
            {
                if (!draggingMouse && mouse.isLeftHeld())
                {
                    frame.setDragging(true);
                    draggingMouse = true;
                }
            }

            frame.drawComponent(context, scaledMx, scaledMy, deltaTicks);
        }

        matrixStack.pop();

        mouse.setLeftClicked(false);
        mouse.setRightClicked(false);
        mouse.setMouseX(scaledMx);
        mouse.setMouseY(scaledMy);

        if (updateDescText)
        {
            return;
        }

        descAnimation.setState(false);
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

        float scale = ClickGuiModule.INSTANCE.getScale();
        final int scaledMx = (int) (mouseX / scale);
        final int scaledMy = (int) (mouseY / scale);

        for (Frame frame : guiFrames)
        {
            frame.mouseClicked(scaledMx, scaledMy, mouseButton);
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

        float scale = ClickGuiModule.INSTANCE.getScale();
        final int scaledMx = (int) (mouseX / scale);
        final int scaledMy = (int) (mouseY / scale);

        for (Frame frame : guiFrames)
        {
            frame.setDragging(false);
            frame.mouseReleased(scaledMx, scaledMy, button);
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
            float scrolledY = (float) (verticalAmount * ClickGuiModule.INSTANCE.getScrollSpeed());
            float y = frame.getY() + scrolledY;
            float minY = -frame.getComponentHeight();
            frame.setY(MathHelper.clamp(y, minY, scrolledY > 0.0f ? 15.0f : Integer.MAX_VALUE));
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

    public void setDescriptionText(String descriptionText)
    {
        this.descriptionText = descriptionText;
        this.updateDescText = true;
        this.descAnimation.setState(true);
    }

    public void addNotification(String notification, int duration)
    {
        notifications.add(new GuiNotification(notification, duration));
    }

    public Theme getTheme()
    {
        return ClickGuiModule.INSTANCE.getTheme();
    }
}