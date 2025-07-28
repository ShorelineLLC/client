package net.shoreline.client.impl.module.combat;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.rotation.RotateMode;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoXPModule extends Toggleable
{
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while using XP")
            .setDefaultValue(true).build();
    Config<RotateMode> rotateConfig = new EnumConfig.Builder<RotateMode>("Rotate")
            .setValues(RotateMode.values())
            .setDescription("Rotate down before using XP")
            .setDefaultValue(RotateMode.OFF).build();
    Config<SilentSwapType> swapConfig = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for swapping to pearls")
            .setDefaultValue(SilentSwapType.HOTBAR).build();

    public AutoXPModule()
    {
        super("AutoXP", "Automatically mends items", GuiCategory.COMBAT);
    }

    @Override
    public String getModuleData()
    {
        return String.valueOf(InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE));
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
            return;
        }

        if (!isPlayerFullDurability())
        {
            disable();
            return;
        }

        int itemSlot = InventoryUtil.getInventorySlot(Items.EXPERIENCE_BOTTLE, swapConfig.getValue());
        if (itemSlot == -1)
        {
            disable();
            return;
        }

        Rotation playerRotation = new Rotation(mc.player);
        Rotation xpThrow = new Rotation(mc.player.getYaw(), 90.0f);

        Managers.ROTATION.setSilentRotation(xpThrow);
        xpThrow.applyToPlayer();

        if (!Managers.INVENTORY.startSwap(itemSlot, swapConfig.getValue()))
        {
            return;
        }

        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, xpThrow.getYaw(), xpThrow.getPitch()));
        Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        Managers.INVENTORY.endSwap();
        playerRotation.applyToPlayer();
    }

    private boolean isPlayerFullDurability()
    {
        for (ItemStack stack : EntityUtil.getEquippedItems(mc.player))
        {
            if (!stack.isEmpty() && stack.isDamaged())
            {
                return false;
            }
        }
        return true;
    }
}
