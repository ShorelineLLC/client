package net.shoreline.client.impl.module.impl;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.interact.Interaction;
import net.shoreline.client.impl.interact.StrictDirection;
import net.shoreline.client.impl.inventory.InventoryUtil;

@Getter
public class ObsidianPlacerModule extends PlacerModule
{
    private Block currentObbyBlock = Blocks.OBSIDIAN;

    public ObsidianPlacerModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public ObsidianPlacerModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected void placeObby(BlockPos placePos)
    {
        int obbySlot = findBestObbySlot();
        if (obbySlot == -1 || !Managers.INVENTORY.startSwap(obbySlot, anticheat.getSwapType()))
        {
            return;
        }

        final Interaction interaction = Interaction.builder()
                .pos(placePos)
                .direction(StrictDirection.getInteractDirection(placePos))
                .hand(Hand.MAIN_HAND)
                .block(currentObbyBlock)
                .build();

        Managers.INTERACT.placeBlock(interaction);
        Managers.INVENTORY.endSwap();
    }

    private int findBestObbySlot()
    {
        int slot = InventoryUtil.getInventorySlot(Items.OBSIDIAN, anticheat.getSwapType());
        if (slot == -1)
        {
            currentObbyBlock = Blocks.ENDER_CHEST;
            return InventoryUtil.getInventorySlot(Items.ENDER_CHEST, anticheat.getSwapType());
        }

        currentObbyBlock = Blocks.OBSIDIAN;
        return slot;
    }
}
