package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.init.Managers;

/**
 * @author Shoreline
 * @since 1.0
 */
public class ChestSwapModule extends ToggleModule {

    Config<Priority> priorityConfig = register(new EnumConfig<>("Priority", "The chestplate material to prioritize", Priority.NETHERITE, Priority.values()));

    public ChestSwapModule() {
        super("ChestSwap", "Automatically swaps chestplate", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable() {
        ItemStack armorStack = mc.player.getInventory().getArmorStack(2);
        int heldSlot = mc.player.getInventory().selectedSlot;
        if (armorStack.getItem() instanceof ArmorItem armorItem
                && armorItem.getSlotType() == EquipmentSlot.CHEST) {
            int elytraSlot = getElytraSlot();
            if (elytraSlot != -1) {
                boolean swap = heldSlot != elytraSlot;
                if (swap) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, heldSlot, SlotActionType.SWAP, mc.player);
                }
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
                if (swap) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            elytraSlot < 9 ? elytraSlot + 36 : elytraSlot, heldSlot, SlotActionType.SWAP, mc.player);
                }
            }
        } else {
            int chestplateSlot = getChestplateSlot();
            if (chestplateSlot != -1) {
                boolean swap = heldSlot != chestplateSlot;
                if (swap) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            chestplateSlot < 9 ? chestplateSlot + 36 : chestplateSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                }
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
                if (swap) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            chestplateSlot < 9 ? chestplateSlot + 36 : chestplateSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                }
            }
        }
        disable();
    }

    private int getChestplateSlot() {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ArmorItem armorItem
                    && armorItem.getSlotType() == EquipmentSlot.CHEST) {
                if (armorItem.getMaterial() == ArmorMaterials.NETHERITE && priorityConfig.getValue() == Priority.NETHERITE) {
                    slot = i;
                    break;
                } else if (armorItem.getMaterial() == ArmorMaterials.DIAMOND && priorityConfig.getValue() == Priority.DIAMOND) {
                    slot = i;
                    break;
                } else {
                    slot = i;
                }
            }
        }
        return slot;
    }

    private int getElytraSlot() {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ElytraItem) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private enum Priority {
        NETHERITE,
        DIAMOND
    }
}
