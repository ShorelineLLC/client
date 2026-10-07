package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DeathProtectionComponent;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.DeathProtectionEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.combat.util.DamageUtil;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.client.impl.module.impl.Priorities;
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
    Config<Boolean> instantReplace = new BooleanConfig.Builder("InstantReplace")
            .setDescription("Instant replaces after popping a totem")
            .setDefaultValue(false).build();
    Config<Boolean> fastSwap = new BooleanConfig.Builder("FastSwap")
            .setDescription("Uses a faster swap method")
            .setDefaultValue(false).build();
    Config<Boolean> mainhandTotem = new BooleanConfig.Builder("MainhandTotem")
            .setDescription("Holds a totem in your mainhand")
            .setDefaultValue(false).build();
    Config<Integer> hotbarTotemSlot = new NumberConfig.Builder<Integer>("HotbarTotemSlot")
            .setMin(0).setMax(8).setDefaultValue(5)
            .setVisible(() -> false)
            .setDescription("The mainhand totem slot").build();

    @Getter
    private boolean isTotemInOffHand, isTotemInMainHand;

    private boolean clearedTotem;

    public AutoTotemModule()
    {
        super("AutoTotem", "Automatically replaces totems when you pop", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return String.valueOf(InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING));
    }

    @EventListener(priority = Priorities.AUTO_TOTEM)
    public void onTick(final TickEvent.Pre event)
    {
        if (checkNull() || !canSwapInventory())
        {
            return;
        }

        ScreenHandler handler = mc.player.currentScreenHandler;
        float playerHealth = DamageUtil.getHealth(mc.player);

        isTotemInMainHand = mainhandTotem.getValue() && playerHealth - DamageUtil.getCrystalDamage(mc.player) <= 2.0;
        if (isTotemInMainHand)
        {
            ItemStack stack = mc.player.getInventory().getStack(hotbarTotemSlot.getValue());
            if (stack.isEmpty() || stack.getItem() != Items.TOTEM_OF_UNDYING)
            {
                swapItemWithSlot(Items.TOTEM_OF_UNDYING, hotbarTotemSlot.getValue(), fastSwap.getValue());
            }

            Managers.INVENTORY.setSelectedSlot(hotbarTotemSlot.getValue());
        }

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

        Item offhandItem = mc.player.getOffHandStack().getItem();
        Item requiredItem = isTotemInOffHand ? Items.TOTEM_OF_UNDYING : modeConfig.getValue().getItem();
        if (offhandItem.equals(requiredItem))
        {
            return;
        }

        int returnSlot = OffhandGappleModule.INSTANCE.getReturnSlot();
        if (offhandItem == Items.ENCHANTED_GOLDEN_APPLE && returnSlot != -1)
        {
            Managers.INVENTORY.pickupSlot(handler, InventoryUtil.OFFHAND_SLOT);
            Managers.INVENTORY.pickupSlot(handler, InventoryUtil.getPacketSlotIndex(handler, returnSlot));
            if (handler.getCursorStack().getItem().equals(requiredItem))
            {
                Managers.INVENTORY.pickupSlot(handler, InventoryUtil.OFFHAND_SLOT);
            } else
            {
                swapItemWithSlot(requiredItem, PlayerInventory.OFF_HAND_SLOT, fastSwap.getValue());
            }

            return;
        }

        swapItemWithSlot(requiredItem, PlayerInventory.OFF_HAND_SLOT, fastSwap.getValue());
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull() || !instantReplace.getValue())
        {
            return;
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getEntity(mc.world) == mc.player
                && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING)
        {
            ItemStack stack = mc.player.getOffHandStack();
            DeathProtectionComponent deathProtectionComponent = stack.get(DataComponentTypes.DEATH_PROTECTION);
            if (deathProtectionComponent == null)
            {
                return;
            }

            stack.decrement(1);
            swapItemWithSlot(Items.TOTEM_OF_UNDYING, PlayerInventory.OFF_HAND_SLOT, fastSwap.getValue());
            clearedTotem = true;
        }

        if (event.getPacket() instanceof InventoryS2CPacket packet
                && packet.getContents().get(45).getItem().equals(Items.AIR) && clearedTotem)
        {
            event.cancel();
            clearedTotem = false;
        }
    }

    @EventListener
    public void onDeathProtect(DeathProtectionEvent event)
    {
        if (instantReplace.getValue())
        {
            event.cancel();
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
