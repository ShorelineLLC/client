package net.shoreline.client.impl.module.combat.trap;

import lombok.Builder;
import lombok.Getter;

import java.util.EnumSet;

@Builder
@Getter
public class TrapSpec
{
    private final EnumSet<TrapLayer> layers;

    @Builder.Default
    private boolean extendFeet = false;

    @Builder.Default
    private boolean extendBody = false;
}

