package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BlockListConfig;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

import java.util.List;

public class XRayModule extends ToggleModule
{
    Config<Integer> opacityConfig = register(new NumberConfig<>("Opacity", "The opacity of the blocks in wallhack", 0, 120, 255));
    Config<Boolean> softReloadConfig = register(new BooleanConfig("SoftReload", "Reloads world renders without causing game interruption", true));
    Config<List<Block>> blocksConfig = register(new BlockListConfig<>("Blocks", "Valid block whitelist for wallhack",
            Blocks.EMERALD_ORE, Blocks.DIAMOND_ORE, Blocks.IRON_ORE,
            Blocks.GOLD_ORE, Blocks.COAL_ORE, Blocks.LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.DEEPSLATE_COPPER_ORE, Blocks.TNT, Blocks.FURNACE,
            Blocks.NETHERITE_BLOCK, Blocks.EMERALD_BLOCK,
            Blocks.DIAMOND_BLOCK, Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK,
            Blocks.COPPER_BLOCK, Blocks.BEACON, Blocks.SPAWNER,
            Blocks.ANCIENT_DEBRIS, Blocks.NETHER_GOLD_ORE));

    public XRayModule()
    {
        super("XRay", "Allows you to see through solid blocks", ModuleCategory.WORLD);
    }

    
}
