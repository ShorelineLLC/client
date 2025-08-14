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
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.util.DamageUtil;
import net.shoreline.client.impl.module.combat.util.TickPriorities;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoTotemModule extends InventorySwapModule
{
    public static AutoTotemModule INSTANCE;

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
    Config<Integer> hotbarTotemSlot = new NumberConfig.Builder<Integer>("HotbarTotemSlot")
            .setMin(0).setMax(8).setDefaultValue(5)
            .setVisible(() -> false)
            .setDescription("The mainhand totem slot").build();

    @Getter
    private boolean isTotemInOffHand, isTotemInMainHand;

    public AutoTotemModule()
    {
        super("AutoTotem", "Automatically replaces totems when you pop", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @EventListener(priority = TickPriorities.AUTO_TOTEM)
    public void onTick(final TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        float playerHealth = DamageUtil.getHealth(mc.player);

        isTotemInMainHand = mainhandTotem.getValue() && playerHealth - DamageUtil.getCrystalDamage(mc.player) <= 0.5;
//      if (isTotemInMainHand)
//      {
//          ItemStack stack = playerInventory.getStack(hotbarTotemSlot.getValue());
//          if (stack.isEmpty() || stack.getItem() != Items.TOTEM_OF_UNDYING)
//          {
//              swapItemWithSlot(Items.TOTEM_OF_UNDYING, hotbarTotemSlot.getValue());
//          }
//
//          mc.player.getInventory().setSelectedSlot(hotbarTotemSlot.getValue());
//      }

        double potentialDamage = 0.5;
        potentialDamage += DamageUtil.getFallDamage(mc.player, mc.player.fallDistance, 1.0f);
        if (damageCheck.getValue())
        {
            potentialDamage += DamageUtil.getCrystalDamage(mc.player);
        }

        isTotemInOffHand = playerHealth - potentialDamage <= healthConfig.getValue();

        if (!isTotemInOffHand && OffhandGappleModule.INSTANCE.isGappleInOffHand())
        {
            return;
        }

        Item offhandItem = isTotemInOffHand ? Items.TOTEM_OF_UNDYING : modeConfig.getValue().getItem();
        if (mc.player.getOffHandStack().getItem().equals(offhandItem))
        {
            return;
        }

        swapItemWithSlot(offhandItem, PlayerInventory.OFF_HAND_SLOT);
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
