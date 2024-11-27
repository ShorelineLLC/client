package net.shoreline.client.impl.module.combat;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linus
 * @since 1.0
 */
public class ReplenishModule extends ToggleModule
{

    Config<Integer> percentConfig = register(new NumberConfig<>("Percent", "The minimum percent of total stack before replenishing", 1, 25, 80));

    // Cached hotbar in case the hotbar slot becomes empty
    private final Map<Integer, ItemStack> hotbarCache = new ConcurrentHashMap<>();

    private final Timer lastDroppedTimer = new CacheTimer();


    public ReplenishModule()
    {
        super("Replenish", "Automatically replaces items in your hotbar", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable()
    {
        hotbarCache.clear();
    }

    @EventListener
    public void onTick(PlayerTickEvent event)
    {
        if (mc.player.age < 10)
        {
            hotbarCache.clear();
            return;
        }

        boolean pauseReplenish = isInInventory() || !lastDroppedTimer.passed(100);

        if (!pauseReplenish)
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty())
                {
                    ItemStack cachedStack = hotbarCache.getOrDefault(i, null);
                    if (cachedStack != null && !cachedStack.isEmpty())
                    {
                        replenishStack(i, cachedStack);
                        break;
                    }
                    continue;
                }

                if (!stack.isStackable())
                {
                    continue;
                }

                double percentage = ((double) stack.getCount() / stack.getMaxCount()) * 100.0;
                if (percentage <= percentConfig.getValue())
                {
                    replenishStack(i, stack);
                    break;
                }
            }
        }

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() && !pauseReplenish)
            {
                continue;
            }

            if (hotbarCache.containsKey(i))
            {
                hotbarCache.replace(i, stack);
            }
            else
            {
                hotbarCache.put(i, stack);
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerActionC2SPacket packet
                && (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM
                || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS))
        {
            lastDroppedTimer.reset();
        }
    }

    private boolean isInInventory()
    {
        return mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof ShulkerBoxScreen || mc.currentScreen instanceof InventoryScreen;
    }

    private void replenishStack(int slot, ItemStack stack)
    {
        if (!InventoryUtil.hasItemInInventory(stack.getItem(), false))
        {
            return;
        }
        // sendModuleError("slot: " + slot + ", stack:" + stack.getName().getString());
        int slot1 = -1;
        for (int i = 9; i < 36; ++i)
        {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.isEmpty())
            {
                continue;
            }
            if (stack.getItem() != itemStack.getItem() || !stack.isStackable())
            {
                continue;
            }
            if (!stack.getName().getString().equals(itemStack.getName().getString()))
            {
                continue;
            }
            if (stack.getItem() instanceof BlockItem blockItem
                    && (!(itemStack.getItem() instanceof BlockItem blockItem1)
                    || blockItem.getBlock() != blockItem1.getBlock()))
            {
                continue;
            }

            slot1 = i;
        }

        if (slot1 != -1)
        {
            if (mc.player.currentScreenHandler.getCursorStack().getItem() != stack.getItem())
            {
                mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            }
            if (mc.player.currentScreenHandler.getCursorStack().getItem() == stack.getItem())
            {
                mc.interactionManager.clickSlot(0, slot + 36, 0, SlotActionType.PICKUP, mc.player);
            }
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            {
                mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            }
        }
    }
}
