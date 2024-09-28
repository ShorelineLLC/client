package net.shoreline.client.impl.manager.world.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shoreline.client.util.Globals;

/**
 * @author linus
 * @since 1.0
 */
public class SoundManager implements Globals
{
    public static final SoundEvent GUI_CLICK = registerSound("gui_click");

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

    private static SoundEvent registerSound(String name)
    {
        Identifier id = Identifier.of(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
