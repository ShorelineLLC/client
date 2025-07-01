package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.helper.DamageUtil;
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
    Config<Boolean> syncConfig = new BooleanConfig.Builder("Sync")
            .setDescription("Syncs totem item client side when you pop")
            .setDefaultValue(false).build();

    Config<Boolean> mainhandTotem = new BooleanConfig.Builder("Totem")
            .setDescription("Holds a totem in your mainhand")
            .setDefaultValue(false).build();
    Config<Integer> slotConfig = new NumberConfig.Builder<Integer>("Slot")
            .setMin(1).setMax(9).setDefaultValue(1)
            .setVisible(() -> mainhandTotem.getValue())
            .setDescription("The slot for mainhand totem").build();
    Config<Void> mainhandConfig = new ConfigGroup.Builder("Mainhand")
            .addAll(mainhandTotem, slotConfig).build();

    private int lastHotbarSlot;
    private Item lastHotbarItem;
    private Item offhandItem;
    private boolean replacing;

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
        if (mainhandTotem.getValue())
        {
            int totemSlot1 = slotConfig.getValue() - 1;
            ItemStack totemSlotStack = mc.player.getInventory().getStack(totemSlot1);
            totemSlot1 += 36;
            if (totemSlotStack.getItem() != Items.TOTEM_OF_UNDYING)
            {
                int n = 35;
                while (n >= 0)
                {
                    if (mc.player.getInventory().getStack(n).getItem() == Items.TOTEM_OF_UNDYING)
                    {
                        int slot = n < 9 ? n + 36 : n;
                        replacing = true;
                        if (mc.player.currentScreenHandler.getCursorStack().getItem() != Items.TOTEM_OF_UNDYING)
                        {
                            mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        }
                        if (mc.player.currentScreenHandler.getCursorStack().getItem() == Items.TOTEM_OF_UNDYING)
                        {
                            mc.interactionManager.clickSlot(0, totemSlot1, 0, SlotActionType.PICKUP, mc.player);
                            // lastTotemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING) - 1;
                        }
                        replacing = false;
                        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty() && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING)
                        {
                            mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        }
                    }
                    n--;
                }
            }

            boolean totemInMainhand = mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING;
            if (!totemInMainhand || lowHealth)
            {
                int totemSlot = -1;
                for (int i = 0; i < 9; i++)
                {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                    {
                        totemSlot = i;
                        break;
                    }
                }
                if (totemSlot != -1)
                {
                    // Managers.INVENTORY.setClientSlot(totemSlot);
                }
            }
        }

        offhandItem = modeConfig.getValue().getItem();
        if (lowHealth)
        {
            offhandItem = Items.TOTEM_OF_UNDYING;
        }

        if (mc.player.getOffHandStack().getItem() == offhandItem)
        {
            return;
        }
        int n = 35;
        if (lastHotbarSlot != -1 && lastHotbarItem != null)
        {
            final ItemStack stack = mc.player.getInventory().getStack(lastHotbarSlot);
            if (stack.getItem().equals(offhandItem) && lastHotbarItem.equals(mc.player.getOffHandStack().getItem()))
            {
                final int tmp = lastHotbarSlot;
                lastHotbarSlot = -1;
                lastHotbarItem = null;
                n = tmp;
            }
        }
        while (n >= 0)
        {
            if (mc.player.getInventory().getStack(n).getItem() == offhandItem)
            {
                if (n < 9)
                {
                    lastHotbarItem = offhandItem;
                    lastHotbarSlot = n;
                }
                int slot = n < 9 ? n + 36 : n;
                replacing = true;
                if (mc.player.currentScreenHandler.getCursorStack().getItem() != offhandItem)
                {
                    mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                }
                if (mc.player.currentScreenHandler.getCursorStack().getItem() == offhandItem)
                {
                    mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
                }
                replacing = false;
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty() && mc.player.getOffHandStack().getItem() == offhandItem)
                {
                    mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
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
