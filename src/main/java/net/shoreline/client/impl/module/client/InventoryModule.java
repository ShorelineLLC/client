package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
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

    Config<Boolean> mapTooltips = new BooleanConfig.Builder("Maps")
            .setDescription("Shows contents of maps in the inventory screen")
            .setDefaultValue(false).build();
    Config<Boolean> shulkerTooltips = new BooleanConfig.Builder("Shulkers")
            .setDescription("Shows contents of shulkers in the inventory screen")
            .setDefaultValue(false).build();
    Config<Void> tooltipsConfig = new ConfigGroup.Builder("Tooltips")
            .addAll(mapTooltips, shulkerTooltips)
            .setDescription("Shows extra tooltips in the inventory screen").build();

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
