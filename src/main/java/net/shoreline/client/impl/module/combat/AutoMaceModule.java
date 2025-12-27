package net.shoreline.client.impl.module.combat;

import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoMaceModule extends Toggleable
{
    Config<Float> maceRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to attack with a mace").build();


    public AutoMaceModule()
    {
        super("AutoMace", "Automatically damages entities with a mace", GuiCategory.COMBAT);
    }

    @EventListener
    public void onClientRotation(ClientRotationEvent event)
    {
        if (Managers.INVENTORY.isHolding(Items.MACE, Hand.MAIN_HAND) &&
                MaceItem.shouldDealAdditionalDamage(mc.player))
        {

        }
    }

    @Override
    public String getModuleData()
    {
        return String.valueOf(mc.player.fallDistance);
    }
}
