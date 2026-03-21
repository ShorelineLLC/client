package net.shoreline.client.impl.module.combat;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.interact.ItemInteraction;
import net.shoreline.client.impl.module.impl.Priorities;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.RotateMode;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoXPModule extends Toggleable
{
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while using XP")
            .setDefaultValue(true).build();
    Config<Boolean> inAirConfig = new BooleanConfig.Builder("InAir")
            .setDescription("Uses XP in the air")
            .setDefaultValue(false).build();
    Config<Integer> bptConfig = new NumberConfig.Builder<Integer>("BottlesPerTick")
            .setMin(1).setDefaultValue(1).setMax(10)
            .setDescription("The number of XP bottles to throw per tick").build();
    Config<RotateMode> rotateConfig = new EnumConfig.Builder<RotateMode>("Rotate")
            .setValues(RotateMode.values())
            .setDescription("Rotate down before using XP")
            .setDefaultValue(RotateMode.OFF).build();

    public AutoXPModule()
    {
        super("AutoXP", "Automatically mends items", GuiCategory.COMBAT);
    }

    @Override
    public String getModuleData()
    {
        return String.valueOf(InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE));
    }

    @EventListener(priority = Priorities.AUTO_XP)
    public void onClientRotation(ClientRotationEvent event)
    {
        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
            return;
        }

        if (rotateConfig.getValue() == RotateMode.NORMAL && event.isCanceled())
        {
            return;
        }

        if (!mc.player.isOnGround() && !inAirConfig.getValue())
        {
            return;
        }

        if (isPlayerFullDurability())
        {
            disable();
            return;
        }

        int itemSlot = InventoryUtil.getItemSlot(Items.EXPERIENCE_BOTTLE);
        if (itemSlot == -1)
        {
            disable();
            return;
        }

        Rotation xpThrow = new Rotation(mc.player.getYaw(), 90.0f);
        switch (rotateConfig.getValue())
        {
            case SILENT -> Managers.ROTATION.setSilentRotation(xpThrow);
            case NORMAL ->
            {
                event.cancel();
                event.setYaw(xpThrow.getYaw());
                event.setPitch(xpThrow.getPitch());
            }
        }

        if (Managers.INVENTORY.startSwap(itemSlot))
        {
            for (int i = 0; i < bptConfig.getValue(); i++)
            {
                Managers.INTERACT.interactItem(new ItemInteraction(Items.EXPERIENCE_BOTTLE,
                        Hand.MAIN_HAND,
                        xpThrow,
                        true));
            }

            Managers.INVENTORY.endSwap();
        }

        if (rotateConfig.getValue() == RotateMode.SILENT)
        {
            Managers.ROTATION.resetSilentRotation();
        }
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
