package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;

/**
 * @author Shoreline
 * @since 1.0
 */
public class ChestSwapModule extends ToggleModule
{

    Config<Priority> priorityConfig = register(new EnumConfig<>("Priority", "The chestplate material to prioritize", Priority.NETHERITE, Priority.values()));
    Config<Boolean> autoFireworkConfig = register(new BooleanConfig("AutoFirework", "Automatically fireworks when swapping to an elytra", false));

    public ChestSwapModule()
    {
        super("ChestSwap", "Automatically swaps chestplate", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        ItemStack armorStack = mc.player.getInventory().getArmorStack(2);
        if (armorStack.getItem() instanceof ArmorItem armorItem
                && armorItem.getSlotType() == EquipmentSlot.CHEST)
        {
            int elytraSlot = getElytraSlot();
            if (elytraSlot != -1)
            {
                Managers.INVENTORY.pickupSlot(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot);
                Managers.INVENTORY.pickupSlot(6);
                Managers.INVENTORY.pickupSlot(elytraSlot < 9 ? elytraSlot + 36 : elytraSlot);
                if (autoFireworkConfig.getValue() && !mc.player.isOnGround())
                {
                    Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startFallFlying();
                    int slot = -1;
                    for (int i = 0; i < 45; i++)
                    {
                        ItemStack stack = mc.player.getInventory().getStack(i);
                        if (stack.getItem() == Items.FIREWORK_ROCKET)
                        {
                            slot = i;
                            break;
                        }
                    }
                    if (slot == -1)
                    {
                        return;
                    }
                    if (slot < 9)
                    {
                        Managers.INVENTORY.setSlot(slot);
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        Managers.INVENTORY.syncToClient();
                    }
                    else
                    {
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(0, mc.player.getInventory().selectedSlot + 36, 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                    }
                }
            }
        }
        else
        {
            int chestplateSlot = getChestplateSlot();
            if (chestplateSlot != -1)
            {
                Managers.INVENTORY.pickupSlot(chestplateSlot < 9 ? chestplateSlot + 36 : chestplateSlot);
                Managers.INVENTORY.pickupSlot(6);
                Managers.INVENTORY.pickupSlot(chestplateSlot < 9 ? chestplateSlot + 36 : chestplateSlot);
            }
        }
        disable();
    }

    private int getChestplateSlot()
    {
        int slot = -1;
        for (int i = 0; i < 36; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ArmorItem armorItem
                    && armorItem.getSlotType() == EquipmentSlot.CHEST)
            {
                if (armorItem.getMaterial() == ArmorMaterials.NETHERITE && priorityConfig.getValue() == Priority.NETHERITE)
                {
                    slot = i;
                    break;
                }
                else if (armorItem.getMaterial() == ArmorMaterials.DIAMOND && priorityConfig.getValue() == Priority.DIAMOND)
                {
                    slot = i;
                    break;
                }
                else
                {
                    slot = i;
                }
            }
        }
        return slot;
    }

    private int getElytraSlot()
    {
        int slot = -1;
        for (int i = 0; i < 36; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ElytraItem)
            {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private enum Priority
    {
        NETHERITE,
        DIAMOND
    }
}