package net.shoreline.client.impl.module.combat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.TickPriorities;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoArmorModule extends InventorySwapModule
{
    Config<Integer> armorPercent = new NumberConfig.Builder<Integer>("ReplaceWhen")
            .setMin(0).setMax(20).setDefaultValue(0).setFormat("%")
            .setDescription("The min armor percent before replacing").build();
    Config<Boolean> fastSwap = new BooleanConfig.Builder("FastSwap")
            .setDescription("Uses a faster swap method")
            .setDefaultValue(false).build();

    public AutoArmorModule()
    {
        super("AutoArmor", "Automatically equips armor", GuiCategory.COMBAT);
    }

    @EventListener(priority = TickPriorities.AUTO_ARMOR)
    public void onTick(final TickEvent.Pre event)
    {
        if (checkNull() || !canSwapInventory())
        {
            return;
        }

        for (int i = 0; i < 4; i++)
        {
            ItemStack armorStack = mc.player.getInventory().getArmorStack(i);
            float percent = ItemUtil.getStackPercent(armorStack) * 100.0f;
            if (armorStack.isEmpty() || percent < armorPercent.getValue())
            {
                swapItemForArmorSlot(i);
            }
        }
    }

    private void swapItemForArmorSlot(int armorSlot)
    {
        for (Item armorItem : getArmorItemVariations(armorSlot))
        {
            int slot = 103 - armorSlot;
            if (swapItemWithSlot(armorItem, slot, fastSwap.getValue()) != -1)
            {
                return;
            }
        }
    }

    public Item[] getArmorItemVariations(int armorSlot)
    {
        String armorName = getArmorName(armorSlot);
        return new Item[]
                {
                        Registries.ITEM.get(Identifier.of("netherite_" + armorName)),
                        Registries.ITEM.get(Identifier.of("diamond_" + armorName)),
                        Registries.ITEM.get(Identifier.of("iron_" + armorName)),
                        Registries.ITEM.get(Identifier.of("golden_" + armorName))
                };
    }

    public String getArmorName(int armorSlot)
    {
        return switch (armorSlot)
        {
            case 0 -> "helmet";
            case 1 -> "chestplate";
            case 2 -> "leggings";
            case 3 -> "boots";
            default -> throw new IllegalStateException("Unexpected value: " + armorSlot);
        };
    }
}
