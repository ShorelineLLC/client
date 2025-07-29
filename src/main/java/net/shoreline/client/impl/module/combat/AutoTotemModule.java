package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.util.DamageUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoTotemModule extends Toggleable
{
    Config<ItemMode> modeConfig = new EnumConfig.Builder<ItemMode>("Mode")
            .setValues(ItemMode.values()).setDefaultValue(ItemMode.TOTEM)
            .setDescription("The item to hold in your offhand").build();
    Config<Float> healthConfig = new NumberConfig.Builder<Float>("Health")
            .setMin(0.0f).setMax(20.0f).setDefaultValue(14.0f)
            .setDescription("Min health before swapping to totem").build();
    Config<Boolean> damageCheck = new BooleanConfig.Builder("Safe")
            .setDescription("Swaps if potential damage will kill the player")
            .setDefaultValue(true).build();

    Config<Boolean> mainhandTotem = new BooleanConfig.Builder("MainhandTotem")
            .setDescription("Holds a totem in your mainhand")
            .setDefaultValue(false).build();

    public AutoTotemModule()
    {
        super("AutoTotem", "Automatically replaces totems when you pop", GuiCategory.COMBAT);
    }

    @EventListener(priority = Integer.MAX_VALUE - 1)
    public void onTick(final TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        float playerHealth = DamageUtil.getHealth(mc.player);
        double potentialDamage = DamageUtil.potentialDamage(mc.player, damageCheck.getValue());

        boolean lowHealth = playerHealth - potentialDamage <= healthConfig.getValue();

        Item offhandItem = modeConfig.getValue().getItem();
        if (lowHealth)
        {
            offhandItem = Items.TOTEM_OF_UNDYING;
        }

        if (mc.player.getOffHandStack().getItem().equals(offhandItem))
        {
            return;
        }

        int n = PlayerInventory.MAIN_SIZE - 1;
        while (n >= 0)
        {
            if (mc.player.getInventory().getStack(n).getItem() == offhandItem)
            {
                Managers.INVENTORY.clickSwap(n, PlayerInventory.OFF_HAND_SLOT, offhandItem);

                if (mc.player.getOffHandStack().getItem().equals(offhandItem))
                {
                    return;
                }
            }

            n--;
        }
    }

    @RequiredArgsConstructor
    @Getter
    private enum ItemMode
    {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.ENCHANTED_GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL);

        private final Item item;
    }
}
