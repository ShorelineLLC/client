package net.shoreline.client.impl.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@RequiredArgsConstructor
@Getter
public class SwapData
{
    private final HotbarCache preHotbar;
    private final int slotFrom, slotTo;

    private final long startTime;

    public SwapData(HotbarCache preHotbar, int slotFrom, int slotTo)
    {
        this(preHotbar, slotFrom, slotTo, System.currentTimeMillis());
    }

    public long getSwapTime()
    {
        return System.currentTimeMillis() - startTime;
    }

    @Getter
    @Setter
    public static class Mutable
    {
        private boolean swapped;
        private int slotFrom, slotTo = InventoryUtil.INVALID_SLOT;

        public void reset()
        {
            swapped = false;
            slotFrom = InventoryUtil.INVALID_SLOT;
            slotTo = InventoryUtil.INVALID_SLOT;
        }

        public ItemStack getItemStack(PlayerInventory playerInventory)
        {
            return playerInventory.getStack(slotTo);
        }
    }
}
