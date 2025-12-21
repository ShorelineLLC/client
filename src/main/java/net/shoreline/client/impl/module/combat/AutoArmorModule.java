package net.shoreline.client.impl.module.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.impl.Priorities;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Map;

public class AutoArmorModule extends InventorySwapModule
{
    Config<Integer> armorPercent = new NumberConfig.Builder<Integer>("ReplaceWhen")
            .setMin(0).setMax(20).setDefaultValue(0).setFormat("%")
            .setDescription("The min armor percent before replacing").build();
    Config<Boolean> fastSwap = new BooleanConfig.Builder("FastSwap")
            .setDescription("Uses a faster swap method")
            .setDefaultValue(false).build();

    private final Map<EquipmentSlot, Integer> equipmentSlots =
            Map.of(EquipmentSlot.HEAD, 5,
                   EquipmentSlot.CHEST, 6,
                   EquipmentSlot.LEGS, 7,
                   EquipmentSlot.FEET, 8);

    public AutoArmorModule()
    {
        super("AutoArmor", "Automatically equips armor", GuiCategory.COMBAT);
    }

    @EventListener(priority = Priorities.AUTO_ARMOR)
    public void onTick(final TickEvent.Pre event)
    {
        if (checkNull() || !canSwapInventory())
        {
            return;
        }

        for (Map.Entry<EquipmentSlot, Integer> slot : equipmentSlots.entrySet())
        {
            if (check(slot.getKey(), slot.getValue()))
            {
                break;
            }
        }
    }

    private boolean check(EquipmentSlot equipment, int slot)
    {
        int armor    = 44 - slot;
        int provided = findArmor(equipment);
        if (provided == -1 || armor == provided || checkArmor(armor))
        {
            return false;
        }

        Item providedItem = mc.player.getInventory().getStack(provided).getItem();
        ScreenHandler handler = mc.player.playerScreenHandler;
        if (fastSwap.getValue())
        {
            Managers.INVENTORY.clickSwap(InventoryUtil.getPacketSlotIndex(handler, provided), slot, providedItem);
        }
        else
        {
            Managers.INVENTORY.pickupSlot(handler, slot);
            Managers.INVENTORY.pickupSlot(handler, InventoryUtil.getPacketSlotIndex(handler, provided));
            Managers.INVENTORY.pickupSlot(handler, slot);
        }

        return true;
    }

    private int findArmor(EquipmentSlot equipment)
    {
        for (int i = 0; i <= 45; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof ArmorItem))
            {
                continue;
            }

            if (getEquipmentSlot(stack).equals(equipment))
            {
                return i;
            }
        }

        return -1;
    }

    private boolean checkArmor(int armor)
    {
        ItemStack armorStack = mc.player.getInventory().getStack(armor);
        float percent = ItemUtil.getStackPercent(armorStack) * 100.0f;
        return !(percent < armorPercent.getValue()) && !armorStack.isEmpty();
    }

    private EquipmentSlot getEquipmentSlot(ItemStack itemStack)
    {
        return itemStack.get(DataComponentTypes.EQUIPPABLE).slot();
    }
}