package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
@Setter
public class EntitySpawnEvent extends Event
{
    private final Vec3d pos;
    private final int entityId;
    private final EntityType<?> type;
}
