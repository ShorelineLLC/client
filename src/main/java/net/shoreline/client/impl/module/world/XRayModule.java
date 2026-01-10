package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.RenderBlockEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Collection;

public class XRayModule extends RenderModule
{
    public static XRayModule INSTANCE;

    Config<Boolean> softReload = new BooleanConfig.Builder("SoftReload")
            .setDescription("Reloads without refreshing renders")
            .setDefaultValue(false).build();

    Config<Collection<Block>> xrayBlocks = new RegistryConfig.Builder<Block>("Blocks")
            .setRegistry(Registries.BLOCK)
            .setValues(Blocks.EMERALD_ORE, Blocks.DIAMOND_ORE, Blocks.IRON_ORE,
                    Blocks.GOLD_ORE, Blocks.COAL_ORE, Blocks.LAPIS_ORE,
                    Blocks.REDSTONE_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                    Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_IRON_ORE,
                    Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_COAL_ORE,
                    Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                    Blocks.DEEPSLATE_COPPER_ORE, Blocks.TNT, Blocks.FURNACE,
                    Blocks.NETHERITE_BLOCK, Blocks.EMERALD_BLOCK,
                    Blocks.DIAMOND_BLOCK, Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK,
                    Blocks.COPPER_BLOCK, Blocks.BEACON, Blocks.SPAWNER,
                    Blocks.ANCIENT_DEBRIS, Blocks.NETHER_GOLD_ORE)
            .setDescription("Blocks whitelist for xray").build();

    public XRayModule()
    {
        super("XRay", "See through blocks", GuiCategory.WORLD);
        INSTANCE = this;

        xrayBlocks.addObserver(v -> reload(softReload.getValue()));
    }

    @Override
    public void onToggle()
    {
        reload(softReload.getValue());
    }

    @EventListener
    public void onRenderBlock(RenderBlockEvent event)
    {
        if (shouldCancelBlockRender(event.getState().getBlock()))
        {
            event.cancel();
        }
    }

    public boolean shouldCancelBlockRender(Block block)
    {
        return !xrayBlocks.getValue().contains(block);
    }
}
