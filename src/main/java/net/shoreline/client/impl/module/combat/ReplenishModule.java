package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
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
    private static ReplenishModule INSTANCE;

    Config<Integer> percentConfig = register(new NumberConfig<>("Percent", "The minimum percent of total stack before replenishing", 1, 25, 80));
    Config<Boolean> resistantConfig = register(new BooleanConfig("AllowResistant", "Refills obsidian with other types of resistant blocks", false));

    // Cached hotbar in case the hotbar slot becomes empty
    private final Map<Integer, ItemStack> hotbarCache = new ConcurrentHashMap<>();

    private final Timer lastDroppedTimer = new CacheTimer();

    public ReplenishModule()
    {
        super("Replenish", "Automatically replaces items in your hotbar", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static ReplenishModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        hotbarCache.clear();
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        hotbarCache.clear();
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof ClientPlayerEntity)
        {
            hotbarCache.clear();
        }
    }

    @EventListener
    public void onTick(PlayerTickEvent event)
    {
        if (mc.options.dropKey.isPressed())
        {
            lastDroppedTimer.reset();
        }

        boolean pauseReplenish = isInInventoryScreen() || !lastDroppedTimer.passed(100);

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
                hotbarCache.replace(i, stack.copy());
            }
            else
            {
                hotbarCache.put(i, stack.copy());
            }
        }
    }

    public boolean isInInventoryScreen()
    {
        return mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof ShulkerBoxScreen || mc.currentScreen instanceof InventoryScreen;
    }

    private void replenishStack(int slot, ItemStack stack)
    {
        int slot1 = -1;
        boolean outOfObsidian = stack.getItem() == Items.OBSIDIAN && InventoryUtil.count(Items.OBSIDIAN) <= 1;
        for (int i = 9; i < 36; ++i)
        {
            ItemStack itemStack = mc.player.getInventory().getStack(i);

            if (itemStack.isEmpty())
            {
                continue;
            }

            if (!isSame(stack, itemStack, outOfObsidian) || !itemStack.isStackable())
            {
                continue;
            }

            slot1 = i;
        }

        if (slot1 != -1)
        {
            // sendModuleError("slot: " + slot + ", stack:" + stack.getName().getString());
            mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, slot + 36, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            {
                mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            }
        }
    }

    public boolean isSame(ItemStack stack1, ItemStack stack2, boolean outOfObsidian)
    {
        if (resistantConfig.getValue() && stack1.getItem() == Items.OBSIDIAN && outOfObsidian)
        {
            return stack2.getItem() == Items.ENDER_CHEST || stack2.getItem() == Items.CRYING_OBSIDIAN;
        }

        else if (stack1.getItem() instanceof BlockItem blockItem
                && (!(stack2.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock()))
        {
            return false;
        }

        else if (!stack1.getName().getString().equals(stack2.getName().getString()))
        {
            return false;
        }

        return stack1.getItem().equals(stack2.getItem());
    }
}
