package net.shoreline.client.impl.module.movement.speed;

import lombok.Getter;

public enum SpeedMode
{
    VANILLA(new Vanilla()),
    B_HOP(new BunnyHop()),
    STRAFE(new Strafe()),
    STRAFE_STRICT(new StrafeNCP());

    @Getter
    private final BaseSpeedFeature feature;

    SpeedMode(BaseSpeedFeature feature)
    {
        this.feature = feature;
    }
}
