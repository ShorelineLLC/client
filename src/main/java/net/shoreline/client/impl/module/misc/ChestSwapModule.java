package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.util.item.ArmorUtil;

public class ChestSwapModule extends Toggleable
{
    Config<Boolean> fireworkConfig = new BooleanConfig.Builder("AutoFirework")
            .setDescription("Automatically uses a firework when swapping to elytra")
            .setDefaultValue(false).build();

    private Item chestplateItem = Items.DIAMOND_CHESTPLATE;

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
            int slot = InventoryUtil.getItemSlot(Items.ELYTRA);
            if (slot != -1)
            {
                Managers.INVENTORY.clickSwap(slot, PlayerInventory.BODY_SLOT, Items.ELYTRA);
            }

            if (fireworkConfig.getValue() && !mc.player.isOnGround())
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                mc.player.startGliding();

                int fireworkSlot = InventoryUtil.getItemSlot(Items.FIREWORK_ROCKET);
                if (fireworkSlot != -1 && Managers.INVENTORY.startSwap(fireworkSlot))
                {
                    Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id,
                            mc.player.getYaw(), mc.player.getPitch()));
                    Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    Managers.INVENTORY.endSwap();
                }
            }
        }

        else if (chestStack.getItem() == Items.ELYTRA)
        {
            int slot = getBestChestplateSlot(playerInventory);
            if (slot != -1)
            {
                Managers.INVENTORY.clickSwap(slot, PlayerInventory.BODY_SLOT, chestplateItem);
            }
        }

        disable();
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
                    chestplateItem = stack.getItem();
                }
            }
        }

        return slot;
    }
}
