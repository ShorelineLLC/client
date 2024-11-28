package net.shoreline.client.impl.manager.world.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shoreline.client.util.Globals;
import net.shoreline.loader.resource.ResourcePackExt;

/**
 * @author linus
 * @since 1.0
 */
public class SoundManager implements Globals
{
    public static final SoundEvent GUI_CLICK = registerSound("gui_click", ".ogg");

    // PM Sounds
    public static final SoundEvent TWITTER = registerSound("twitter", ".ogg");
    public static final SoundEvent IOS = registerSound("ios", ".ogg");
    public static final SoundEvent DISCORD = registerSound("discord", ".ogg");
    public static final SoundEvent STEAM = registerSound("steam", ".ogg");

    /**
     * @param sound
     */
    public void playSound(final SoundEvent sound)
    {
        playSound(sound, 1.2f, 0.75f);
    }

    public void playSound(final SoundEvent sound, float volume, float pitch)
    {
        if (mc.player != null)
        {
            mc.executeSync(() -> mc.player.playSound(sound, volume, pitch));
        }
    }

    private static SoundEvent registerSound(String name,
                                            String extension)
    {
        ResourcePackExt.REGISTERED_SOUND_FILES.add(name + extension);

        Identifier id = Identifier.of("shoreline", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
