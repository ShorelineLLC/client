package net.shoreline.client.impl.inventory;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.shoreline.client.impl.module.client.InventoryModule;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class InventoryUtil
{
    public static final int INVALID_SLOT = -1;
    public static final int OFFHAND_SLOT = 45;

    public boolean isInInventoryScreen()
    {
        return MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen
                || MinecraftClient.getInstance().currentScreen instanceof ShulkerBoxScreen
                || MinecraftClient.getInstance().currentScreen instanceof InventoryScreen;
    }

    public ItemSlot getItemSlot(Function<ItemStack, Boolean> stackFilter)
    {
        return getItemSlot(stackFilter, InventoryModule.INSTANCE.getSilentSwapType());
    }

    public ItemSlot getItemSlot(Function<ItemStack, Boolean> stackFilter, SilentSwapType type)
    {
        PlayerInventory inv = MinecraftClient.getInstance().player.getInventory();

        ItemStack itemStack = null;
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
                    itemStack = stack;
                }
            }
        }

        return new ItemSlot(bestSlot, itemStack);
    }

    public ItemSlot getItem(Item item)
    {
        return getItem(item, InventoryModule.INSTANCE.getSilentSwapType());
    }

    public ItemSlot getItem(Item item, SilentSwapType type)
    {
        return type == SilentSwapType.INVENTORY ? getInventorySlot(item) : getHotbarItem(item);
    }

    public int getItemSlot(Item item)
    {
        return getItemSlot(item, InventoryModule.INSTANCE.getSilentSwapType());
    }

    public int getItemSlot(Item item, SilentSwapType type)
    {
        return type == SilentSwapType.INVENTORY ? getInventorySlot(item).getSlot() : getHotbarItem(item).getSlot();
    }

    public ItemSlot getInventorySlot(Item item)
    {
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++)
        {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem().equals(item))
            {
                return new ItemSlot(i, stack);
            }
        }

        return new ItemSlot(INVALID_SLOT, ItemStack.EMPTY);
    }

    public ItemSlot getHotbarItem(Predicate<ItemStack> predicate)
    {
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++)
        {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (predicate.test(stack))
            {
                return new ItemSlot(i, stack);
            }
        }

        return new ItemSlot(INVALID_SLOT, ItemStack.EMPTY);
    }

    public ItemSlot getHotbarItem(Item item)
    {
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++)
        {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (stack.getItem().equals(item))
            {
                return new ItemSlot(i, stack);
            }
        }

        return new ItemSlot(INVALID_SLOT, ItemStack.EMPTY);
    }

    public int find(Predicate<ItemStack> tester)
    {
        for (int i = 0; i < 45; i++)
        {
            ItemStack stack = MinecraftClient.getInstance().player.getInventory().getStack(i);
            if (tester.test(stack))
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
                count += stack.getCount();
            }
        }

        ItemStack offhand = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
        if (offhand.getItem().equals(item))
        {
            count += offhand.getCount();
        }

        return count;
    }

    public int getPacketSlotIndex(ScreenHandler handler, int slot)
    {
        if (handler instanceof PlayerScreenHandler)
        {
            if (slot == PlayerInventory.OFF_HAND_SLOT)
            {
                return OFFHAND_SLOT;
            }

            if (slot > 100)
            {
                return 108 - slot;
            }

            return slot < PlayerInventory.getHotbarSize() ? slot + PlayerInventory.MAIN_SIZE : slot;
        }

        final List<Slot> slots = handler.slots;
        for (int id = 0; id < slots.size(); id++)
        {
            Slot s = slots.get(id);
            if (s.getIndex() == slot)
            {
                return id;
            }
        }

        return slot;
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
