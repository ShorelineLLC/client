package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.hud.ChatMessageEvent;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;

public class PMSoundModule extends ToggleModule
{
    private static PMSoundModule INSTANCE;
    Config<PMSounds> soundsConfig = register(new EnumConfig<>("Sounds",
            "The sound to play for PM", PMSounds.TWITTER, PMSounds.values()));

    public PMSoundModule()
    {
        super("PMSound", "Plays a sound for private messages", ModuleCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    public static PMSoundModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onChatMessage(ChatMessageEvent event)
    {
        String chatText = event.getText().getString();
        if (!chatText.startsWith("<")
                && (chatText.contains("whispers:") // 2b2t pm
                || chatText.contains("says:"))) // cc pm
        {
            Managers.SOUND.playSound(switch (soundsConfig.getValue())
            {
                case TWITTER -> SoundManager.TWITTER;
                case IOS -> SoundManager.IOS;
                case DISCORD -> SoundManager.DISCORD;
                case STEAM -> SoundManager.STEAM;
            });
        }
    }

    public PMSounds getPMSound()
    {
        return soundsConfig.getValue();
    }

    public enum PMSounds
    {
        TWITTER,
        IOS,
        DISCORD,
        STEAM
    }
}
