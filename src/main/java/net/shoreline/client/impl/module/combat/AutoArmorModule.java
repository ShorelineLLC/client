package net.shoreline.client.impl.module.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.inventory.InventoryManager;
import net.shoreline.client.impl.module.impl.Priorities;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.*;
import java.util.function.Consumer;

public class AutoArmorModule extends InventorySwapModule
{
    public static AutoArmorModule INSTANCE;
    Config<Integer> delay = new NumberConfig.Builder<Integer>("Delay")
            .setMin(0).setMax(500).setDefaultValue(250)
            .setDescription("The delay between clicks").build();
    Config<Integer> armorPercent = new NumberConfig.Builder<Integer>("ReplaceWhen")
            .setMin(0).setMax(20).setDefaultValue(0).setFormat("%")
            .setDescription("The min armor percent before replacing").build();
    Config<Boolean> fastSwap = new BooleanConfig.Builder("FastSwap")
            .setDescription("Uses a faster swap method")
            .setDefaultValue(false).build();
    Config<Boolean> multiClick = new BooleanConfig.Builder("MultiClick")
            .setDefaultValue(true).build();

    private final Map<EquipmentSlot, Integer> equipmentSlots =
            Map.of(EquipmentSlot.HEAD, 5,
                   EquipmentSlot.CHEST, 6,
                   EquipmentSlot.LEGS, 7,
                   EquipmentSlot.FEET, 8);

    private final Timer timer = new NanoTimer();
    private final Queue<Click> clicks = new LinkedList<>();

    public AutoArmorModule()
    {
        super("AutoArmor", "Automatically equips armor", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @EventListener(priority = Priorities.AUTO_ARMOR)
    public void onTick(final TickEvent.Pre event)
    {
        if (checkNull() || !canSwapInventory())
        {
            return;
        }

        if (clicks.isEmpty())
        {
            for (Map.Entry<EquipmentSlot, Integer> slot : equipmentSlots.entrySet())
            {
                if (check(slot.getKey(), slot.getValue()))
                {
                    break;
                }
            }
        }

        if (timer.hasPassed(delay.getValue()))
        {
            Click click = clicks.poll();
            if (click != null)
            {
                click.execute();
                timer.reset();
            }
        }
    }

    private boolean check(EquipmentSlot equipment, int slot)
    {
        int armor = 44 - slot;
        ItemStack armorStack = mc.player.getInventory().getStack(armor);
        if (equipment == EquipmentSlot.CHEST && armorStack.getItem() == Items.ELYTRA)
        {
            return false;
        }

        int provided = findArmor(equipment);
        if (provided == -1 || armor == provided || checkArmor(armorStack))
        {
            return false;
        }

        ItemStack providedStack = mc.player.getInventory().getStack(provided);
        ScreenHandler handler = mc.player.playerScreenHandler;
        int providedSlot = InventoryUtil.getPacketSlotIndex(handler, provided);
        if (fastSwap.getValue())
        {
            Click click = new Click(slot, -1);
            queueClick(click.setFast(providedSlot, providedStack.getItem()));
        }
        else
        {
            queueClick(slot, providedSlot);
            if (!multiClick.getValue())
            {
                queueClick(providedSlot, -1);
                queueClick(slot, -1);
            }
        }

        return true;
    }

    private void queueClick(int slot, int target)
    {
        queueClick(new Click(slot, target));
    }

    private void queueClick(Click click)
    {
        clicks.add(click);
    }

    public int findArmor(EquipmentSlot equipment)
    {
        return InventoryUtil.find(stack ->
        {
            if (!(stack.getItem() instanceof ArmorItem))
            {
                return false;
            }

            return getEquipmentSlot(stack).equals(equipment);
        });
    }

    private boolean checkArmor(ItemStack armorStack)
    {
        float percent = ItemUtil.getStackPercent(armorStack) * 100.0f;
        return !(percent < armorPercent.getValue()) && !armorStack.isEmpty();
    }

    private EquipmentSlot getEquipmentSlot(ItemStack itemStack)
    {
        return itemStack.get(DataComponentTypes.EQUIPPABLE).slot();
    }

    private class Click
    {
        private final int slot;
        private final int target;

        private int fastSlot;
        private Item fastItem;

        public Click(int slot, int target)
        {
            this.slot = slot;
            this.target = target;
            this.fastSlot = -1;
        }

        public void execute()
        {
            InventoryManager inventory = Managers.INVENTORY;
            ScreenHandler handler = mc.player.playerScreenHandler;
            if (slot != -1 && fastSlot != -1)
            {
                inventory.clickSwap(fastSlot, slot, fastItem);
                return;
            }

            if (slot != -1)
            {
                inventory.pickupSlot(handler, slot);
                if (target != -1 && multiClick.getValue())
                {
                    inventory.pickupSlot(handler, target);
                    inventory.pickupSlot(handler, slot);
                }
            }
        }

        public Click setFast(int fastSlot, Item fastItem)
        {
            this.fastSlot = fastSlot;
            this.fastItem = fastItem;
            return this;
        }
    }
}