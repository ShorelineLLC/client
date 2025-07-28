package net.shoreline.client.impl.inventory;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@UtilityClass
public class InventoryUtil
{
    public boolean isInInventoryScreen()
    {
        return MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen
                || MinecraftClient.getInstance().currentScreen instanceof ShulkerBoxScreen
                || MinecraftClient.getInstance().currentScreen instanceof InventoryScreen;
    }

    public int getInventorySlot(Item item, SilentSwapType swapType)
    {
        return swapType == SilentSwapType.INVENTORY ? getInventorySlot(item) : getHotbarSlot(item);
    }

    public int getInventorySlot(Item item)
    {
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++)
        {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().equals(item))
            {
                return i;
            }
        }

        return -1;
    }

    public int getHotbarSlot(Item item)
    {
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++)
        {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (stack.getItem().equals(item))
            {
                return i;
            }
        }

        return -1;
    }

    public int getItemCount(Item item)
    {
        int count = 0;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++)
        {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().equals(item))
            {
                count++;
            }
        }

        ItemStack offhand = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
        if (offhand.getItem().equals(item))
        {
            count++;
        }

        return count;
    }

    public int getPacketSlotIndex(int slot)
    {
        if (slot == PlayerInventory.OFF_HAND_SLOT)
        {
            return 45;
        }

        if (slot > PlayerInventory.MAIN_SIZE)
        {
            return slot - PlayerInventory.MAIN_SIZE + 1;
        }

        return slot < PlayerInventory.getHotbarSize() ? slot + PlayerInventory.MAIN_SIZE : slot;
    }
}
