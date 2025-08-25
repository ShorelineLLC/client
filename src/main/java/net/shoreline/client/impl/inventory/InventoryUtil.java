package net.shoreline.client.impl.inventory;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.client.impl.module.client.AnticheatModule;

import java.util.function.Function;

@UtilityClass
public class InventoryUtil
{
    public static final int INVALID_SLOT = -1;

    public boolean isInInventoryScreen()
    {
        return MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen
                || MinecraftClient.getInstance().currentScreen instanceof ShulkerBoxScreen
                || MinecraftClient.getInstance().currentScreen instanceof InventoryScreen;
    }

    public int getItemSlot(Function<ItemStack, Boolean> stackFilter)
    {
        return getItemSlot(stackFilter, AnticheatModule.INSTANCE.getSilentSwapType());
    }

    public int getItemSlot(Function<ItemStack, Boolean> stackFilter, SilentSwapType type)
    {
        PlayerInventory inv = MinecraftClient.getInstance().player.getInventory();

        int bestSlot = INVALID_SLOT;
        int bestScore = -1;

        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++)
        {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty() || !stackFilter.apply(stack))
            {
                continue;
            }

            int rank = getMaterialRank(stack);
            if (rank > bestScore)
            {
                bestScore = rank;
                if (type == SilentSwapType.INVENTORY || i < PlayerInventory.getHotbarSize())
                {
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    public int getItemSlot(Item item)
    {
        return getItemSlot(item, AnticheatModule.INSTANCE.getSilentSwapType());
    }

    public int getItemSlot(Item item, SilentSwapType type)
    {
        return type == SilentSwapType.INVENTORY ? getInventorySlot(item) : getHotbarSlot(item);
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

        return INVALID_SLOT;
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

        return INVALID_SLOT;
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

    private int getMaterialRank(ItemStack stack)
    {
        String key = stack.getItem().getTranslationKey();
        if (key.contains("netherite")) return 600;
        if (key.contains("diamond"))   return 500;
        if (key.contains("iron"))      return 400;
        if (key.contains("gold"))      return 300;
        if (key.contains("stone"))     return 200;
        if (key.contains("wood"))      return 100;

        return 0;
    }
}
