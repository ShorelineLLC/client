package net.shoreline.client.impl.rotation;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
public class ClientRotationEvent extends Event
{
    @Getter
    @Setter
    private Rotation rotation;

    public ClientRotationEvent(Rotation rotation)
    {
        this.rotation = rotation;
    }

    public void setYaw(float yaw)
    {
        rotation.setYaw(yaw);
    }

    public void setPitch(float pitch)
    {
        rotation.setPitch(pitch);
    }
}
