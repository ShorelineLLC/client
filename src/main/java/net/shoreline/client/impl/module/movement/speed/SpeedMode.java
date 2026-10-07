package net.shoreline.client.impl.module.movement.speed;

import lombok.Getter;
import net.shoreline.client.impl.module.movement.SpeedModule;

public enum SpeedMode
{
    VANILLA(new Vanilla()),
    B_HOP(new BunnyHop()),
    STRAFE(new Strafe()),
    STRAFE_STRICT(new StrafeNCP());

    @Getter
    private final BaseSpeedFeature<SpeedModule> feature;

    SpeedMode(BaseSpeedFeature<SpeedModule> feature)
    {
        this.feature = feature;
    }
}
