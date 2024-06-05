package net.shoreline.client.impl.event.world;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class PlaySoundEvent extends Event
{

    private final Vec3d pos;
    private final SoundEvent event;
    private final SoundCategory category;

    public PlaySoundEvent(Vec3d pos, SoundEvent event, SoundCategory category)
    {
        this.pos = pos;
        this.event = event;
        this.category = category;
    }

    public Vec3d getPos()
    {
        return pos;
    }

    public SoundEvent getSoundEvent()
    {
        return event;
    }

    public SoundCategory getCategory()
    {
        return category;
    }
}
