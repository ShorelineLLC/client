package net.shoreline.client.impl.module.combat;


import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.util.TickPriorities;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class OffhandGappleModule extends InventorySwapModule
{
    public static OffhandGappleModule INSTANCE;

    Config<Boolean> swordConfig = new BooleanConfig.Builder("Swords")
            .setDescription("Allows gapples in offhand when holding a sword")
            .setDefaultValue(true).build();
    Config<Boolean> toolsConfig = new BooleanConfig.Builder("Tools")
            .setDescription("Allows gapples in offhand when holding a tool")
            .setDefaultValue(true).build();
    Config<Boolean> totemConfig = new BooleanConfig.Builder("Totems")
            .setDescription("Allows gapples in offhand when holding a totem")
            .setDefaultValue(true).build();

    @Getter
    private boolean isGappleInOffHand;

    public OffhandGappleModule()
    {
        super("OffhandGapple", "Swaps golden apples into your offhand", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @EventListener(priority = TickPriorities.OFFHAND)
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || AutoTotemModule.INSTANCE.isTotemInOffHand())
        {
            return;
        }

        ItemStack stack = mc.player.getMainHandStack();
        isGappleInOffHand = canEatWhileHolding(stack.getItem()) && mc.options.useKey.isPressed();

        if (!isGappleInOffHand || mc.player.getOffHandStack().getItem().equals(Items.ENCHANTED_GOLDEN_APPLE))
        {
            return;
        }

        swapItemWithSlot(Items.ENCHANTED_GOLDEN_APPLE, PlayerInventory.OFF_HAND_SLOT);
    }

    private boolean canEatWhileHolding(Item item)
    {
        return swordConfig.getValue() && ItemUtil.isSword(item)
                || toolsConfig.getValue() && ItemUtil.isTool(item)
                || totemConfig.getValue() && item == Items.TOTEM_OF_UNDYING;
    }
}
