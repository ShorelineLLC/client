package net.shoreline.client.gui.titlescreen;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.shoreline.client.Shoreline;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.titlescreen.particle.ParticleManager;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowManager;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowParticle;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.client.TitleScreenModule;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class ShorelineMenuScreen extends Screen
{
    private final List<MenuButton> buttons;
    private static ParticleManager<SnowParticle> snowManager;
    private final ClickGuiScreen clickGuiScreen = ClickGuiScreen.INSTANCE;
    private boolean renderingGui;

    public ShorelineMenuScreen()
    {
        super(Text.of("Shoreline-MainMenu"));
        buttons = new ArrayList<>();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height)
    {
        super.resize(client, width, height);
        snowManager.reset();
    }

    @Override
    protected void init()
    {
        super.init();
        if (snowManager == null)
        {
            snowManager = TitleScreenModule.INSTANCE.getManager();
        }
        else
        {
            snowManager.reset();
        }

        resetButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        context.fill(0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), 0xFF000000);
        snowManager.update();
        snowManager.render(context);

        double mX = renderingGui ? -1 : mouseX;
        double mY = renderingGui ? -1 : mouseY;
        for (MenuButton button : buttons)
        {
            button.render(context, mX, mY, delta);
        }

        if (renderingGui)
        {
            clickGuiScreen.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (renderingGui)
        {
            clickGuiScreen.mouseClicked(mouseX, mouseY, button);
        }
        else
        {
            buttons.forEach(menuButton -> menuButton.mouseClicked(mouseX, mouseY, button));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (renderingGui)
        {
            clickGuiScreen.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (renderingGui)
        {
            clickGuiScreen.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == ClickGuiModule.INSTANCE.getKeybind().getValue().getKeycode())
        {
            renderingGui = true;
            Window window = client.getWindow();
            ClickGuiModule.INSTANCE.setFadeState(true);
            clickGuiScreen.init(client, window.getScaledWidth(), window.getScaledHeight());
        }
        else if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            renderingGui = false;
            clickGuiScreen.reset(); // idk
        }
        else if (renderingGui)
        {
            clickGuiScreen.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (renderingGui)
        {
            clickGuiScreen.charTyped(chr, modifiers);
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    public void resetButtons()
    {
        buttons.clear();
        Window window = client.getWindow();
        float scaledWidth  = window.getScaledWidth();
        float scaledHeight = window.getScaledHeight();
        float spacing = 10;

        List<MenuButton> allButtons = new ArrayList<>();
        allButtons.add(new MenuButton(I18n.translate("menu.singleplayer").toUpperCase(Locale.ROOT), () -> client.setScreen(new SelectWorldScreen(this)), 0, 0));
        allButtons.add(new MenuButton(I18n.translate("menu.multiplayer").toUpperCase(Locale.ROOT), () -> client.setScreen(new MultiplayerScreen(this)), 0, 0));
        allButtons.add(new MenuButton(I18n.translate("menu.options").toUpperCase(Locale.ROOT).replace(".", ""), () -> client.setScreen(new OptionsScreen(this, client.options)), 0, 0));

        if (hasIAS())
        {
            allButtons.add(new MenuButton("Accounts".toUpperCase(), () -> client.setScreen(getAccountScreen(this)), 0, 0));
        }

        allButtons.add(new MenuButton(I18n.translate("menu.quit").toUpperCase(Locale.ROOT), client::scheduleStop, 0, 0));

        float totalWidth = 0;
        for (MenuButton button : allButtons)
        {
            totalWidth += button.getWidth();
        }

        totalWidth += spacing * (allButtons.size() - 1);

        float startX = (scaledWidth - totalWidth) / 2;
        float centerY = (scaledHeight / 2) + 60;

        float currentX = startX;
        for (MenuButton button : allButtons)
        {
            buttons.add(new MenuButton(button.getName(), button.getRunnable(), currentX, centerY));
            currentX += button.getWidth() + spacing;
        }
    }

    public static void setSnowManager(SnowManager manager)
    {
        ShorelineMenuScreen.snowManager = manager;
        snowManager.reset();
    }

    public boolean hasIAS()
    {
        try
        {
            Class.forName("ru.vidtu.ias.IASMinecraft");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    public Screen getAccountScreen(Screen parent)
    {
        try
        {
            String screenName = "ru.vidtu.ias.screen.AccountScreen";
            Class<?> screen = Class.forName(screenName);
            Constructor<?> ctr = screen.getDeclaredConstructor(Screen.class);
            ctr.setAccessible(true);
            return (Screen) ctr.newInstance(parent);
        }
        catch (ClassNotFoundException
               | InstantiationException
               | IllegalAccessException
               | InvocationTargetException
               | NoSuchMethodException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
