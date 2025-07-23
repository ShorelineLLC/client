package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.util.item.ArmorUtil;

public class ChestSwapModule extends Toggleable
{
    Config<Boolean> fireworkConfig = new BooleanConfig.Builder("AutoFirework")
            .setDescription("Automatically uses a firework when swapping to elytra")
            .setDefaultValue(false).build();

    public ChestSwapModule()
    {
        super("ChestSwap", "Swaps elytra and chestplate", GuiCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (checkNull())
        {
            return;
        }

        PlayerInventory playerInventory = mc.player.getInventory();
        ItemStack chestStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chestStack.isIn(ItemTags.CHEST_ARMOR))
        {
            int slot = InventoryUtil.getInventorySlot(Items.ELYTRA);
            if (slot != -1)
            {
                swapChestSlot(slot);
            }
        }

        else if (chestStack.getItem() == Items.ELYTRA)
        {
            int slot = getBestChestplateSlot(playerInventory);
            if (slot != -1)
            {
                swapChestSlot(slot);
            }

            if (fireworkConfig.getValue() && !mc.player.isOnGround())
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                mc.player.startGliding();

                int fireworkSlot = InventoryUtil.getInventorySlot(Items.FIREWORK_ROCKET);
                if (fireworkSlot == -1 || Managers.INVENTORY.startSwap(fireworkSlot, SilentSwapType.INVENTORY))
                {
                    return;
                }

                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                Managers.INVENTORY.endSwap();
            }
        }

        disable();
    }

    private void swapChestSlot(int slot)
    {
        int toSlot = slot < PlayerInventory.getHotbarSize() ? slot + 36 : slot;
    }

    private int getBestChestplateSlot(PlayerInventory playerInventory)
    {
        int slot = -1;

        double armorValue = 0.0;
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++)
        {
            ItemStack stack = playerInventory.getStack(i);
            if (stack.isIn(ItemTags.CHEST_ARMOR))
            {
                double value = ArmorUtil.getArmorValue(stack);
                if (value > armorValue)
                {
                    slot = i;
                    armorValue = value;
                }
            }
        }

        return slot;
    }
}
