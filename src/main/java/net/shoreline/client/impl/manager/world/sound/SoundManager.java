package net.shoreline.client.impl.manager.world.sound;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.shoreline.client.util.Globals;
import org.jetbrains.annotations.Nullable;

/**
 * @author linus
 * @since 1.0
 */
public class SoundManager implements Globals
{
    //
    public static final WeightedSoundSet EMPTY_SOUND_SET;

    static
    {
        EMPTY_SOUND_SET = new WeightedSoundSet(Identifier.of("minecraft",
                "intentionally_empty"), null);
    }

    /**
     * @param sound
     */
    public void playSound(final SoundEvents sound)
    {
        mc.getSoundManager().play(new SoundInstance()
        {
            @Override
            public Identifier getId()
            {
                return sound.getId();
            }

            @Nullable
            @Override
            public WeightedSoundSet getSoundSet(net.minecraft.client.sound.SoundManager soundManager)
            {
                return EMPTY_SOUND_SET;
            }

            @Override
            public Sound getSound()
            {
                return new Sound(sound.getId(), ConstantFloatProvider.create(1.0f),
                        ConstantFloatProvider.create(1.0f), 1,
                        Sound.RegistrationType.SOUND_EVENT, false, false, 16);
            }

            @Override
            public SoundCategory getCategory()
            {
                return SoundCategory.PLAYERS;
            }

            @Override
            public boolean isRepeatable()
            {
                return false;
            }

            @Override
            public boolean isRelative()
            {
                return false;
            }

            @Override
            public int getRepeatDelay()
            {
                return 0;
            }

            @Override
            public float getVolume()
            {
                return 1.0f;
            }

            @Override
            public float getPitch()
            {
                return 1.0f;
            }

            @Override
            public double getX()
            {
                return mc.player != null ? (float) mc.player.getX() : 0;
            }

            @Override
            public double getY()
            {
                return mc.player != null ? (float) mc.player.getY() : 0;
            }

            @Override
            public double getZ()
            {
                return mc.player != null ? (float) mc.player.getZ() : 0;
            }

            @Override
            public AttenuationType getAttenuationType()
            {
                return AttenuationType.LINEAR;
            }
        });
    }
}
