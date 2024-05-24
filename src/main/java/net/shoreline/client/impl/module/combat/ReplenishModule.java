package net.shoreline.client.impl.module.combat;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.player.SetStackEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;

/**
 * @author linus
 * @since 1.0
 */
public class ReplenishModule extends ToggleModule {

    Config<Integer> percentConfig = register(new NumberConfig<>("Percent", "The minimum percent of total stack before replenishing", 0, 25, 80));

    // Cached hotbar in case the hotbar slot becomes empty
    private Item[] hotbar = new Item[9];

    public ReplenishModule() {
        super("Replenish", "Automatically replaces items in your hotbar", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable() {
        hotbar = new Item[9];
    }

    @EventListener
    public void onTick(PlayerTickEvent event) {
        if (mc.currentScreen != null) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == null || stack.isEmpty()) {
                Item item = hotbar[i];
                if (item != null && item != Items.AIR) {
                    replenishItem(item, i);
                }
            }
            else {
                double stackPercent = ((float) stack.getCount() / stack.getMaxCount()) * 100.0f;
                if (stackPercent <= percentConfig.getValue()) {
                    replenishStack(stack, i);
                }
            }
        }
    }

    @EventListener
    public void onSetStack(SetStackEvent event) {
        if (event.getSlot() < 9)
        {
            if (event.getStack().isEmpty() && !isInInventory()) {
                return;
            }
            hotbar[event.getSlot()] = event.getStack().getItem();
        }
    }

    private boolean isInInventory() {
        return mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof ShulkerBoxScreen || mc.currentScreen instanceof InventoryScreen;
    }

    private void replenishItem(Item item, int hotbarSlot) {
        int total = 0;
        while (total < item.getMaxCount())
        {
            ReplenishData i = searchReplenishItem(item);
            if (i == null)
            {
                break;
            }
            replenishInternal(i.slot(), hotbarSlot);
            total += i.count();
        }
    }

    private void replenishStack(ItemStack stack, int hotbarSlot) {
        int total = stack.getCount();
        while (total < stack.getMaxCount())
        {
            ReplenishData i = searchReplenishStack(stack);
            if (i == null)
            {
                break;
            }
            replenishInternal(i.slot(), hotbarSlot);
            total += i.count();
        }
    }

    private ReplenishData searchReplenishItem(Item item) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack1 = mc.player.getInventory().getStack(i);
            if (item instanceof BlockItem blockItem && (!(stack1.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock())) {
                continue;
            }
            if (item != stack1.getItem()) {
                continue;
            }
            return new ReplenishData(i, stack1.getCount());
        }
        return null;
    }

    private ReplenishData searchReplenishStack(ItemStack stack) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack1 = mc.player.getInventory().getStack(i);
            // We cannot merge stacks if they don't have the same name
            if (!stack.getName().getString().equals(stack1.getName().getString())) {
                continue;
            }
            if (stack.getItem() instanceof BlockItem blockItem && (!(stack1.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock())) {
                continue;
            }
            if (stack.getItem() != stack1.getItem()) {
                continue;
            }
            return new ReplenishData(i, stack1.getCount());
        }
        return null;
    }

    private void replenishInternal(int i, int hotbarSlot) {
        Managers.INVENTORY.pickupSlot(i);
        boolean replace = !mc.player.getInventory().getStack(hotbarSlot + 36).isEmpty();
        Managers.INVENTORY.pickupSlot(hotbarSlot + 36);
        if (replace) {
            boolean prevStack = !mc.player.getInventory().getStack(i).isEmpty();
            Managers.INVENTORY.pickupSlot(prevStack ? Managers.INVENTORY.findEmptySlot() : i);
        }
    }

    public record ReplenishData(int slot, int count) {}
}
