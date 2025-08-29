package net.shoreline.client.impl.module.combat;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.inventory.HotbarCache;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class ReplenishModule extends Toggleable
{
    Config<Integer> stackPercent = new NumberConfig.Builder<Integer>("Min")
            .setMin(0).setMax(99).setDefaultValue(0).setFormat("%")
            .setDescription("The minimum percent of stack before refill").build();

    private HotbarCache cache;

    public ReplenishModule()
    {
        super("Replenish", "Refills items in the hotbar", GuiCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        HotbarCache newCache = new HotbarCache(mc.player.getInventory());
        if (shouldReplenish())
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = newCache.getStack(i);
                double percentage = ((double) stack.getCount() / stack.getMaxCount()) * 100;
                if (percentage < stackPercent.getValue() || stack.isEmpty())
                {
                    ItemStack cached = cache.getStack(i);
                    if (cached != null && !cached.isEmpty() && (cached.getItem() == stack.getItem() || stack.isEmpty()))
                    {
                        ItemStack result = stack.isEmpty() ? cached : stack;
                        int slot = getSlot(result);
                        if (slot == -1)
                        {
                            return;
                        }

                        int slot1 = InventoryUtil.getPacketSlotIndex(mc.player.playerScreenHandler, i);
                        Managers.INVENTORY.clickSwap(slot, slot1, result.getItem());
                    }
                }
            }
        }
        else
        {
            updateCache(newCache);
            return;
        }

        cache = newCache;
    }

    private boolean shouldReplenish()
    {
        return cache != null
                && !(mc.currentScreen instanceof GenericContainerScreen)
                && !(mc.currentScreen instanceof ShulkerBoxScreen)
                && !(mc.currentScreen instanceof InventoryScreen);
    }

    private void updateCache(HotbarCache newCache)
    {
        if (cache == null)
        {
            cache = newCache;
        }
        else
        {
            cache = null;
        }
    }

    /**
     * Finds the best stack to use to replenish with. If we find a stack over the delta,
     * we return that one, otherwise we return the biggest.
     */
    private int getSlot(ItemStack stack)
    {
        int slot = -1;
        int biggest = 0;
        int delta = stack.getMaxCount() - stack.getCount();

        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 9; i < PlayerInventory.MAIN_SIZE; i++)
        {
            ItemStack iStack = inventory.getStack(i);
            if (iStack.getItem().equals(stack.getItem()))
            {
                int count = iStack.getCount();
                if (count > delta)
                {
                    return i;
                }
                else if (count > biggest)
                {
                    biggest = count;
                    slot = i;
                }
            }
        }

        return slot;
    }
}
