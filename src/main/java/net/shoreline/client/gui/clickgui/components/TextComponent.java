package net.shoreline.client.gui.clickgui.components;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

public class TextComponent extends FrameComponent
{
    private float typedX;
    private final Deque<Character> buffer = new ArrayDeque<>();

    @Getter
    private boolean typing;

    private boolean showInsertionPoint;
    private final Timer timer = new NanoTimer();

    private final int mouseButton;

    private final Supplier<String> value;
    private final Consumer<String> setter;

    private final IntPredicate charFilter;

    public TextComponent(Frame frame,
                         float x,
                         float y,
                         float frameWidth,
                         float frameHeight,
                         int mouseButton,
                         IntPredicate charFilter,
                         Supplier<String> value,
                         Consumer<String> setter)
    {
        super(frame, x, y, frameWidth, frameHeight);
        this.mouseButton = mouseButton;
        this.charFilter = charFilter;
        this.value = value;
        this.setter = setter;
        collectToBuffer(value.get());
    }

    public TextComponent(Frame frame,
                         float x,
                         float y,
                         float frameWidth,
                         float frameHeight,
                         int mouseButton,
                         Supplier<String> value,
                         Consumer<String> setter)
    {
        this(frame, x, y, frameWidth, frameHeight, mouseButton, c -> true, value, setter);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        String buffer = bufferToString();
        String formattedText1 = buffer.isEmpty() ? getInsertionPoint(true) : buffer + getInsertionPoint(false);
        enableScissor(context, getX() + 3.0f, getY(), getX() + width, getY() + height);

        context.getMatrices().push();
        context.getMatrices().translate(-typedX, 0.0f, 0.0f);
        drawText(context, formattedText1, getX() + 3.0f, getY() + 4.0f, theme.getTextColor());
        context.getMatrices().pop();

        disableScissor(context);
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int button)
    {
        if (Mouse.isHovering(mouseX, mouseY, getX(), getY(), width, height) && button == mouseButton)
        {
            if (typing)
            {
                setter.accept(bufferToString());
            }

            typing = !typing;
        } else
        {
            typing = false;
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
        if (!typing)
        {
            return;
        }

        switch (keyCode)
        {
            case GLFW.GLFW_KEY_ENTER ->
            {
                setter.accept(bufferToString());
                typing = false;
            }
            case GLFW.GLFW_KEY_BACKSPACE ->
            {
                if (!buffer.isEmpty())
                {
                    buffer.removeLast();
                    updateScrolling();
                }
            }
            case GLFW.GLFW_KEY_ESCAPE ->
            {
                updateBuffer(value.get());
                typing = false;
            }
        }
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        if (typing && charFilter.test(chr))
        {
            buffer.addLast(chr);
            updateScrolling();
        }
    }

    private void updateScrolling()
    {
        float scale = ClickGuiModule.INSTANCE.getScale();
        int textW = getTextWidth(bufferToString());
        float componentWidth = width - (6.0f * scale);

        if (textW - typedX > componentWidth)
        {
            typedX = textW - componentWidth;
        }
        else if (textW - typedX < componentWidth && typedX > 0)
        {
            typedX = Math.max(0, textW - componentWidth);
        }
    }

    private void collectToBuffer(String text)
    {
        text.chars().filter(charFilter).forEach(c -> buffer.addLast((char) c));
    }

    public void updateBuffer(String text)
    {
        buffer.clear();
        collectToBuffer(text);
        updateScrolling();
    }

    private String bufferToString()
    {
        StringBuilder sb = new StringBuilder(buffer.size());
        buffer.forEach(ch -> sb.append(ch.charValue()));
        return sb.toString();
    }

    private String getInsertionPoint(boolean style)
    {
        if (timer.hasPassed(250))
        {
            showInsertionPoint = !showInsertionPoint;
            timer.reset();
        }

        if (showInsertionPoint && typing)
        {
            return style ? "|" : "_";
        }

        return "";
    }
}
