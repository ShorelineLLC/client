package net.shoreline.client.impl.module.combat.trap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;

import java.util.EnumSet;
import java.util.NavigableSet;

public class TrapModule extends ObsidianPlacerModule
{
    protected final TrapPositionCalc trapPos = new TrapPositionCalc();

    protected static final TrapSpec FEET_TRAP_SPEC = TrapSpec.builder()
            .layers(EnumSet.of(TrapLayer.FEET))
            .extendBody(false)
            .extendFeet(false)
            .build();

    public TrapModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public TrapModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public NavigableSet<BlockPos> getFeetTrap(Box boundingBox)
    {
        trapPos.calcTrap(boundingBox, FEET_TRAP_SPEC);
        return trapPos.getTrapPositions();
    }
}
