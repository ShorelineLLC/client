package net.shoreline.client.impl.module.combat;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.interact.ItemInteraction;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoPotModule extends Toggleable
{
    public AutoPotModule()
    {
        super("AutoPot", "Throws pots for you", GuiCategory.COMBAT);
    }

    @EventListener
    public void onUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        int slot = InventoryUtil.getItemSlot(Items.SPLASH_POTION);
        if (slot == -1)
        {
            return;
        }

        Rotation rotation = new Rotation(mc.player);
        rotation.setPitch(90.0f);
        Managers.ROTATION.setSilentRotation(rotation);

        if (!Managers.INVENTORY.startSwap(slot))
        {
            return;
        }

        Managers.INTERACT.interactItem(new ItemInteraction(
                Items.SPLASH_POTION,
                Hand.MAIN_HAND,
                rotation,
                true));

        Managers.INVENTORY.endSwap();
        disable();
    }
}
