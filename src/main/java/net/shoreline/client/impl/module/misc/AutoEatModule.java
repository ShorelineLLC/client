package net.shoreline.client.impl.module.misc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class AutoEatModule extends ToggleModule
{
    //
    Config<Float> hungerConfig = register(new NumberConfig<>("Hunger", "The minimum hunger level before eating", 1.0f, 19.0f, 20.0f));

    /**
     *
     */
    public AutoEatModule()
    {
        super("AutoEat", "Automatically eats when losing hunger",
                ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onDisable()
    {
        mc.options.useKey.setPressed(false);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        //
        HungerManager hungerManager = mc.player.getHungerManager();
        if (hungerManager.getFoodLevel() <= hungerConfig.getValue())
        {
            int slot = getFoodSlot();
            if (slot == -1)
            {
                return;
            }
            if (slot == 45)
            {
                mc.player.setCurrentHand(Hand.OFF_HAND);
            }
            else
            {
                Managers.INVENTORY.setClientSlot(slot);
            }
            mc.options.useKey.setPressed(true);
        }
        else
        {
            mc.options.useKey.setPressed(false);
        }
    }

    public int getFoodSlot()
    {
        int foodLevel = -1;
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem().getComponents().contains(DataComponentTypes.FOOD))
            {
                if (stack.getItem() == Items.PUFFERFISH
                        || stack.getItem() == Items.CHORUS_FRUIT)
                {
                    continue;
                }
                int hunger = stack.getItem().getComponents().get(DataComponentTypes.FOOD).nutrition();
                if (hunger > foodLevel)
                {
                    slot = i;
                    foodLevel = hunger;
                }
            }
        }
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem().getComponents().contains(DataComponentTypes.FOOD))
        {
            if (offhand.getItem() == Items.PUFFERFISH
                    || offhand.getItem() == Items.CHORUS_FRUIT)
            {
                return slot;
            }
            int hunger = offhand.getItem().getComponents().get(DataComponentTypes.FOOD).nutrition();
            if (hunger > foodLevel)
            {
                slot = 45;
            }
        }
        return slot;
    }
}
