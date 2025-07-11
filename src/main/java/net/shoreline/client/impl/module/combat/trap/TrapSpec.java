package net.shoreline.client.impl.module.combat.trap;

import lombok.Builder;
import lombok.Getter;

import java.util.EnumSet;

@Builder
@Getter
public class TrapSpec
{
    private final EnumSet<TrapLayer> layers;
    private boolean extendFeet;
    private boolean extendBody;
}

