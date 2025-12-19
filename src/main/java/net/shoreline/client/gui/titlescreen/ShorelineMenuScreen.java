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
import net.shoreline.client.gui.titlescreen.particle.ParticleManager;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowManager;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowParticle;
import net.shoreline.client.impl.module.client.TitleScreenModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class ShorelineMenuScreen extends Screen
{
    private final List<MenuButton> buttons;
    private ParticleManager<SnowParticle> snowManager;

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

        for (MenuButton button : buttons)
        {
            button.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        buttons.forEach(menuButton -> menuButton.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
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

        MenuButton singleplayerButton = new MenuButton(I18n.translate("menu.singleplayer").toUpperCase(Locale.ROOT), () -> client.setScreen(new SelectWorldScreen(this)), 0, 0);
        MenuButton multiplayerButton  = new MenuButton(I18n.translate("menu.multiplayer").toUpperCase(Locale.ROOT), () -> client.setScreen(new MultiplayerScreen(this)), 0, 0);
        MenuButton optionsButton      = new MenuButton(I18n.translate("menu.options").toUpperCase(Locale.ROOT).replace(".", ""), () -> client.setScreen(new OptionsScreen(this, client.options)), 0, 0);
        MenuButton quitButton         = new MenuButton(I18n.translate("menu.quit").toUpperCase(Locale.ROOT), client::scheduleStop, 0, 0);

        MenuButton[] allButtons = {singleplayerButton, multiplayerButton, optionsButton, quitButton};

        float totalWidth = 0;
        for (MenuButton button : allButtons)
        {
            totalWidth += button.getWidth();
        }

        totalWidth += spacing * (allButtons.length - 1);

        float startX = (scaledWidth - totalWidth) / 2;
        float centerY = (scaledHeight / 2) + 60;

        float currentX = startX;
        for (MenuButton button : allButtons)
        {
            buttons.add(new MenuButton(button.getName(), button.getRunnable(), currentX, centerY));
            currentX += button.getWidth() + spacing;
        }
    }
}
