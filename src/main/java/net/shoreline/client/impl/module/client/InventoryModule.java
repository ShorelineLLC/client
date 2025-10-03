package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.inventory.SilentSwapType;

@Getter
public class InventoryModule extends Concurrent
{
    public static InventoryModule INSTANCE;

    Config<SilentSwapType> silentSwap = new EnumConfig.Builder<SilentSwapType>("SilentSwap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for silent swapping to items")
            .setDefaultValue(SilentSwapType.HOTBAR).build();
    Config<Boolean> assumeEnchanted = new BooleanConfig.Builder("AssumeBestArmor")
            .setDescription("Assumes that all enemy armor is max enchanted")
            .setDefaultValue(false).build();

    public InventoryModule()
    {
        super("Inventory", "Manages inventory interactions", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    public SilentSwapType getSilentSwapType()
    {
        return silentSwap.getValue();
    }
}
