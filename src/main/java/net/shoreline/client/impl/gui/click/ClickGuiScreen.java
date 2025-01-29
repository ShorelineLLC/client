package net.shoreline.client.impl.gui.click;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.component.ScissorStack;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.gui.click.impl.config.setting.*;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;

/**
 * @author linus
 * @see ClickGuiModule
 * @since 1.0
 */
public class ClickGuiScreen extends Screen implements Globals
{
    // mouse position
    public static int MOUSE_X;
    public static int MOUSE_Y;
    // mouse states
    public static boolean MOUSE_RIGHT_CLICK;
    public static boolean MOUSE_RIGHT_HOLD;
    public static boolean MOUSE_LEFT_CLICK;
    public static boolean MOUSE_LEFT_HOLD;
    //
    public static final ScissorStack SCISSOR_STACK = new ScissorStack();
    private final List<CategoryFrame> frames = new CopyOnWriteArrayList<>();
    private final ClickGuiModule module;
    //
    private String text;
    private final Animation animation = new Animation(false, 200L, Easing.LINEAR);

    private boolean shouldCloseOnEsc = true;

    /**
     *
     */
    public ClickGuiScreen(ClickGuiModule module)
    {
        super(Text.literal("ClickGui"));
        this.module = module;
        float x = 15.0f;
        for (ModuleCategory category : ModuleCategory.values())
        {
            CategoryFrame frame = new CategoryFrame(category, x, 15.0f);
            frames.add(frame);
            x += frame.getWidth() + (2.0f * ClickGuiModule.CLICK_GUI_SCALE);
        }
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        context.getMatrices().push();
        renderAndScaleGUI(context);
        boolean hovering = false;
        for (CategoryFrame frame : frames)
        {
            if (frame.isWithin(mouseX, mouseY) && MOUSE_LEFT_HOLD && checkDragging())
            {
                frame.setDragging(true);
            }
            frame.render(context, mouseX, mouseY, delta);

            for (ModuleButton moduleButton : frame.getModuleButtons())
            {
                if (moduleButton.isWithin(mouseX, mouseY))
                {
                    text = moduleButton.getModule().getDescription();
                    hovering = true;
                    break;
                }

                if (!moduleButton.isOpen())
                {
                    continue;
                }

                for (ConfigButton<?> configButton : moduleButton.getConfigButtons())
                {
                    if (configButton.isWithin(mouseX, mouseY))
                    {
                        text = configButton.getConfig().getDescription();
                        hovering = true;
                        break;
                    }
                }
            }
        }
        context.getMatrices().pop();

        if (ClickGuiModule.getInstance().getDescriptions())
        {
            animation.setState(hovering);
            if (animation.getFactor() > 0.01f)
            {
                context.getMatrices().scale(ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.CLICK_GUI_SCALE, 0.0f);
                float j = 1.0f / ClickGuiModule.CLICK_GUI_SCALE;
                int width = RenderManager.textWidth(text);
                int hoverAlpha = (int) (60 * animation.getFactor());
                int unfilledColor = ClickGuiModule.getInstance().fixTransparency(new Color(0, 0, 0, 50 + hoverAlpha).getRGB());
                RenderManager.rect(context.getMatrices(), (mouseX + 10.0f) * j, (mouseY - 8.0f) * j, (width + 4.0f), (RenderManager.textHeight() + 2.0f) * j, unfilledColor);
                RenderManager.renderText(context, text, (mouseX + 12.0f) * j, (mouseY - 6.0f) * j, ColorUtil.fixTransparency(-1, (float) animation.getFactor()));
                context.getMatrices().scale(j, j, 0.0f);
            }
        }

        // update mouse state
        MOUSE_LEFT_CLICK = false;
        MOUSE_RIGHT_CLICK = false;
        // update mouse position
        MOUSE_X = mouseX;
        MOUSE_Y = mouseY;
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     * @return
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            MOUSE_LEFT_CLICK = true;
            MOUSE_LEFT_HOLD = true;
        }
        else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            MOUSE_RIGHT_CLICK = true;
            MOUSE_RIGHT_HOLD = true;
        }
        for (CategoryFrame frame : frames)
        {
            frame.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     * @param button the mouse button number
     * @return
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            MOUSE_LEFT_HOLD = false;
        }
        else if (button == 1)
        {
            MOUSE_RIGHT_HOLD = false;
        }
        for (CategoryFrame frame : frames)
        {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     * @return
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        for (CategoryFrame frame : frames)
        {
            float scrolledY = (float) (frame.getY() + verticalAmount * ClickGuiModule.getInstance().getScrollSpeed());
            scrolledY = MathHelper.clamp(scrolledY, -(frame.getTotalHeight() - 10.0f),15.0f);
            frame.setPos(frame.getX(), scrolledY);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     * @return
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {

        // TODO: hard reset GUI in case something fails
        if (keyCode == GLFW_KEY_R && (modifiers & GLFW_MOD_CONTROL) != 0)
        {
            // System.out.println("Hard reset");
        }

        // Insanity
        shouldCloseOnEsc = true;
        for (CategoryFrame frame : frames)
        {
            if (frame.getSearchButton() != null && frame.getSearchButton().isTyping())
            {
                shouldCloseOnEsc = false;
                continue;
            }
            for (ModuleButton moduleButton : frame.getModuleButtons())
            {
                for (ConfigButton<?> configButton : moduleButton.getConfigButtons())
                {
                    if (configButton instanceof BindButton bindButton && bindButton.isListening()
                            || configButton instanceof TextButton textButton && textButton.isTyping()
                            || configButton instanceof ColorButton colorButton && colorButton.isTyping()
                            || configButton instanceof SliderButton sliderButton && sliderButton.isTyping())
                    {
                        shouldCloseOnEsc = false;
                    }
                }
            }
        }
        for (CategoryFrame frame : frames)
        {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        for (CategoryFrame frame : frames)
        {
            frame.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    /**
     * @return
     */
    @Override
    public boolean shouldPause()
    {
        return false;
    }

    /**
     *
     */
    @Override
    public void close()
    {
        module.disable();
        //
        MOUSE_LEFT_CLICK = false;
        MOUSE_LEFT_HOLD = false;
        MOUSE_RIGHT_CLICK = false;
        MOUSE_RIGHT_HOLD = false;
        super.close();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return shouldCloseOnEsc;
    }

    private void renderAndScaleGUI(DrawContext context)
    {
        int backgroundColor = ClickGuiModule.getInstance().fixTransparency(0x66000000);

        RenderManager.rect(
                context.getMatrices(),
                0.0D,
                0.0D,
                context.getScaledWindowWidth(),
                context.getScaledWindowHeight(),
                backgroundColor
        );

        float currentProgress = ClickGuiModule.getInstance().getScaleFactor(); // [0.0 .. 1.0]

        if (currentProgress == 1.0F)
        {
            return;
        }

        // Translate the scale to the center of the screen
        context.getMatrices().translate(context.getScaledWindowHeight(), context.getScaledWindowHeight() / 2.0F, 0.0F);

        float goal = currentProgress * 0.2F;
        context.getMatrices().scale(0.8F + goal, 0.8F + goal, 0.0F);

        // Translate back
        context.getMatrices().translate(-context.getScaledWindowHeight(), -context.getScaledWindowHeight() / 2.0F, 0.0F);
    }

    private boolean checkDragging()
    {
        for (CategoryFrame frame : frames)
        {
            if (frame.isDragging())
            {
                return false;
            }
        }
        return true;
    }

    public List<CategoryFrame> getCategoryFrames()
    {
        return frames;
    }
}
