package net.shoreline.client.impl.gui.click.impl.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.Serializable;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.impl.gui.click.component.Frame;
import net.shoreline.client.impl.gui.click.impl.SearchButton;
import net.shoreline.client.impl.gui.click.impl.config.setting.ColorButton;
import net.shoreline.client.impl.gui.click.impl.config.setting.ConfigButton;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.string.EnumFormatter;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Configuration {@link Frame} (aka the "ClickGui" frames) which
 * allows the user to configure a {@link Module}'s {@link Config} values.
 *
 * @author linus
 * @see Frame
 * @see Module
 * @see Config
 * @since 1.0
 */
public class CategoryFrame extends Frame implements Serializable<CategoryFrame>
{
    //
    private final String name;
    private final ModuleCategory category;
    // private final Identifier categoryIcon;
    // module components
    private final List<ModuleButton> moduleButtons =
            new CopyOnWriteArrayList<>();
    // global module offset
    private float off, inner;
    private boolean open;
    private boolean drag;
    //
    private final Animation categoryAnimation = new Animation(false, 200, Easing.CUBIC_IN_OUT);

    private SearchButton searchButton;

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public CategoryFrame(ModuleCategory category, float x, float y,
                         float width, float height)
    {
        super(x, y, width, height);
        this.category = category;
        // this.categoryIcon = new Identifier("shoreline", "icon/" + category.name().toLowerCase() + ".png");
        this.name = EnumFormatter.formatEnum(category);
        for (Module module : Managers.MODULE.getModules())
        {
            if (module.getCategory() == category)
            {
                moduleButtons.add(new ModuleButton(module, this, x, y));
            }
        }
        if (category == ModuleCategory.CLIENT)
        {
            searchButton = new SearchButton(this, x, y);
        }
        setOpen(true);
    }

    /**
     * @param category
     * @param x
     * @param y
     */
    public CategoryFrame(ModuleCategory category, float x, float y)
    {
        this(category, x, y, 105.0f, 15.0f);
    }

    /**
     * @param context
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, float mouseX, float mouseY, float delta)
    {
        setHeight(RenderManager.textHeight() + 6.0f);
        if (drag)
        {
            x += ClickGuiScreen.MOUSE_X - px;
            y += ClickGuiScreen.MOUSE_Y - py;
        }
        // draw the component
        // context.drawTexture(categoryIcon, (int) (x + 2.0f), (int) (y + 1.0f), 0, 0, 12, 12, 12, 12);
        fheight = 2.0f * ClickGuiModule.CLICK_GUI_SCALE;
        if (searchButton != null)
        {
            fheight += searchButton.getHeight() + (6.0f * ClickGuiModule.CLICK_GUI_SCALE);
        }
        for (ModuleButton moduleButton : moduleButtons)
        {
            // account for button height
            fheight += moduleButton.getHeight() + ClickGuiModule.CLICK_GUI_SCALE;
            if (moduleButton.getScaledTime() < 0.01f)
            {
                continue;
            }
            fheight += 3.0f * ClickGuiModule.CLICK_GUI_SCALE * moduleButton.getScaledTime();
            for (ConfigButton<?> configButton : moduleButton.getConfigButtons())
            {
                if (!configButton.getConfig().isVisible())
                {
                    continue;
                }
                // config button height may vary
                fheight += configButton.getHeight() * moduleButton.getScaledTime();
                if (configButton instanceof ColorButton colorPicker && colorPicker.getScaledTime() > 0.01f)
                {
                    fheight += colorPicker.getPickerHeight() * colorPicker.getScaledTime() * moduleButton.getScaledTime();
                }
            }
        }
        rect(context, ClickGuiModule.getInstance().getColor(1.7f));

        int whiteText = -1;
        drawStringScaled(context, name, x + (3.0f * ClickGuiModule.CLICK_GUI_SCALE), y + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);
        if (categoryAnimation.getFactor() > 0.01f)
        {
            // Enabling scissor during the animation zoom in process causes some weird visual bugs
            boolean canScissor = ClickGuiModule.getInstance().getScaleFactor() == 1.0F;

            float y1 = y;
            if (searchButton != null)
            {
                y1 += searchButton.getHeight() + (6.0f * ClickGuiModule.CLICK_GUI_SCALE);
            }
            if (canScissor)
            {
                enableScissor(x, y + (height * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE), y + (ClickGuiModule.CLICK_GUI_SCALE * height) + fheight * categoryAnimation.getFactor());
            }

            int fillColor = ClickGuiModule.getInstance().fixTransparency(0x77000000);
            fill(context, x, y + (height * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE), fheight, fillColor);

            if (ClickGuiModule.getInstance().getBlur() && ClickGuiModule.getInstance().getScaleFactor() == 1.0f)
            {
                mc.options.getMenuBackgroundBlurriness().setValue(7);
                mc.gameRenderer.renderBlur(delta);
                mc.getFramebuffer().beginWrite(false);
                mc.options.getMenuBackgroundBlurriness().setValue(5);
            }

            if (searchButton != null)
            {
                searchButton.render(context, x + ClickGuiModule.CLICK_GUI_SCALE,
                        y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE),
                        mouseX, mouseY, delta);
            }

            off = y1 + (height * ClickGuiModule.CLICK_GUI_SCALE) + ClickGuiModule.CLICK_GUI_SCALE;
            inner = off;
            for (ModuleButton moduleButton : moduleButtons)
            {
                moduleButton.render(context, x + ClickGuiModule.CLICK_GUI_SCALE, inner + ClickGuiModule.CLICK_GUI_SCALE, mouseX, mouseY, delta);
                off += (float) ((moduleButton.getHeight() + ClickGuiModule.CLICK_GUI_SCALE) * categoryAnimation.getFactor());
                inner += moduleButton.getHeight() + ClickGuiModule.CLICK_GUI_SCALE;
            }

            if (canScissor)
            {
                disableScissor();
            }
        }
        // update previous position
        px = ClickGuiScreen.MOUSE_X;
        py = ClickGuiScreen.MOUSE_Y;
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isWithin(mouseX, mouseY))
        {
            open = !open;
            categoryAnimation.setState(open);

            if (ClickGuiModule.getInstance().getSounds())
            {
                Managers.SOUND.playSound(SoundManager.GUI_CLICK);
            }
        }
        if (isOpen())
        {
            for (ModuleButton button : moduleButtons)
            {
                button.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
        if (searchButton != null)
        {
            searchButton.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param mouseButton
     */
    @Override
    public void mouseReleased(double mouseX, double mouseY, int mouseButton)
    {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        drag = false;
        if (isOpen())
        {
            for (ModuleButton button : moduleButtons)
            {
                button.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
        if (searchButton != null)
        {
            searchButton.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {
        super.keyPressed(keyCode, scanCode, modifiers);
        if (isOpen())
        {
            for (ModuleButton button : moduleButtons)
            {
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        if (searchButton != null)
        {
            searchButton.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char character, int modifiers)
    {
        super.charTyped(character, modifiers);
        if (isOpen())
        {
            for (ModuleButton button : moduleButtons)
            {
                button.charTyped(character, modifiers);
            }
        }
        if (searchButton != null)
        {
            searchButton.charTyped(character, modifiers);
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("category", category.name());
        obj.addProperty("x", getX());
        obj.addProperty("y", getY());
        obj.addProperty("open", isOpen());
        return obj;
    }

    @Override
    public CategoryFrame fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("x") && jsonObj.has("y") && jsonObj.has("open"))
        {
            JsonElement xElement = jsonObj.get("x");
            JsonElement yElement = jsonObj.get("y");
            JsonElement openElement = jsonObj.get("open");
            setPos(xElement.getAsFloat(), yElement.getAsFloat());
            setOpen(openElement.getAsBoolean());
        }
        return null;
    }

    /**
     * @param mx
     * @param my
     * @return
     */
    public boolean isWithinTotal(float mx, float my)
    {
        return isMouseOver(mx, my, x, y, width * ClickGuiModule.CLICK_GUI_SCALE, getTotalHeight());
    }

    /**
     * Update global offset
     *
     * @param in The offset
     */
    public void offset(float in)
    {
        off += in;
        inner += in;
    }

    /**
     * @return
     */
    public ModuleCategory getCategory()
    {
        return category;
    }

    public boolean isOpen()
    {
        return open && categoryAnimation.getFactor() > 0.1f;
    }

    public void setOpen(boolean open)
    {
        this.open = open;
        categoryAnimation.setState(open);
    }

    /**
     * Gets the total height of the frame
     *
     * @return The total height
     */
    public float getTotalHeight()
    {
        return (height * ClickGuiModule.CLICK_GUI_SCALE) + fheight;
    }

    /**
     * @return
     */
    public List<ModuleButton> getModuleButtons()
    {
        return moduleButtons;
    }

    public void setDragging(boolean drag)
    {
        this.drag = drag;
    }

    public boolean isDragging()
    {
        return drag;
    }

    public SearchButton getSearchButton()
    {
        return searchButton;
    }
}
