package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.world.ExplosionUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xgraza
 * @since 1.0
 */
public final class AutoTotemModule extends ToggleModule
{
    EnumConfig<OffhandItem> itemConfig = new EnumConfig<>("Item", "The item to wield in your offhand", OffhandItem.TOTEM, OffhandItem.values());
    NumberConfig<Float> healthConfig = new NumberConfig<>("Health", "The health required to fall below before swapping to a totem", 0.0f, 14.0f, 20.0f);
    Config<Boolean> gappleConfig = new BooleanConfig("OffhandGapple", "If to equip a golden apple if holding down the item use button", true);
    Config<Boolean> crappleConfig = new BooleanConfig("Crapple", "If to use a normal golden apple if Absorption is present", true);
    Config<Boolean> lethalConfig = new BooleanConfig("Lethal", "Calculate lethal damage sources", false);
    Config<Boolean> fastConfig = new BooleanConfig("FastSwap", "Swaps items to offhand", true);
    Config<Boolean> waitConfig = new BooleanConfig("Wait", "If to wait for the previous inventory action to complete", true);
    Config<Boolean> inventoryCheckConfig = new BooleanConfig("InventoryCheck", "", true);
    Config<Boolean> debugConfig = new BooleanConfig("Debug", "If to debug on death", false);

    private int lastHotbarSlot, lastTotemCount;
    private Item lastHotbarItem;
    private boolean replacing;

    public AutoTotemModule()
    {
        super("AutoTotem", "Automatically replenishes the totem in your offhand", ModuleCategory.COMBAT);
    }

    @Override
    public String getModuleData()
    {
        return String.valueOf(Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING));
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        lastHotbarSlot = -1;
        lastHotbarItem = null;
        lastTotemCount = 0;
        replacing = false;
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event)
    {
        // Do not attempt to replace totems inside another container GUI
        if (!(mc.player.currentScreenHandler instanceof PlayerScreenHandler) && inventoryCheckConfig.getValue())
        {
            return;
        }
        // Get the item to wield in our offhand, and make sure we are already not holding the item
        final Item itemToWield = getItemToWield();
        if (mc.player.getOffHandStack().getItem().equals(itemToWield))
        {
            replacing = false;
            return;
        }

        if (replacing && waitConfig.getValue())
        {
            return;
        }

        // Find the item in our inventory
        final int itemSlot = getSlotFor(itemToWield);
        if (itemSlot != -1)
        {
            replacing = true;
            if (itemSlot < 9)
            {
                lastHotbarItem = itemToWield;
                lastHotbarSlot = itemSlot;
            }
            // Do another quick swap (equivalent to hovering over an item & pressing F)
            if (fastConfig.getValue()) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        itemSlot < 9 ? itemSlot + 36 : itemSlot, 40, SlotActionType.SWAP, mc.player);
            } else {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        45, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
                }
            }
            // Don't ask about the - 1, I don't want to talk about it
            lastTotemCount = Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING) - 1;
        }
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event)
    {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof HealthUpdateS2CPacket packet && packet.getHealth() <= 0.0f && debugConfig.getValue())
        {
            final Set<String> reasons = new LinkedHashSet<>();

            if (lastTotemCount <= 0)
            {
                reasons.add("no_totems");
            }

            if (mc.player.currentScreenHandler.syncId != 0)
            {
                reasons.add("gui_fail(" + mc.player.currentScreenHandler.syncId + ")");
            }

            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            {
                reasons.add("cursor_stack=" + mc.player.currentScreenHandler.getCursorStack().getItem());
            }

            if (replacing)
            {
                reasons.add("replacing(stuck?)");
            }

            if (!reasons.isEmpty())
            {
                sendModuleMessage("Possible failure reasons: " + String.join(", ", reasons));
            }
            else
            {
                final int totemCount = Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING);
                sendModuleMessage("Could not figure out possible reasons. meta:{totemCount=" + totemCount + ", matchesCache=" + (totemCount == lastTotemCount) + ", cached=" + lastTotemCount +"}");
            }
        }
        if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && packet.getEntity(mc.world) == mc.player) {
            final int itemSlot = getSlotFor(Items.TOTEM_OF_UNDYING);
            if (itemSlot != -1)
            {
                if (itemSlot < 9)
                {
                    lastHotbarItem = Items.TOTEM_OF_UNDYING;
                    lastHotbarSlot = itemSlot;
                }
                // Do another quick swap (equivalent to hovering over an item & pressing F)
                if (fastConfig.getValue()) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            itemSlot < 9 ? itemSlot + 36 : itemSlot, 40, SlotActionType.SWAP, mc.player);
                } else {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            45, 0, SlotActionType.PICKUP, mc.player);
                    if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                                itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    }
                }
                // Don't ask about the - 1, I don't want to talk about it
                lastTotemCount = Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING) - 1;
            }
        }
    }

    private int getSlotFor(final Item item)
    {
        if (lastHotbarSlot != -1 && lastHotbarItem != null)
        {
            final ItemStack stack = mc.player.getInventory().getStack(lastHotbarSlot);
            if (stack.getItem().equals(item) && lastHotbarItem.equals(mc.player.getOffHandStack().getItem()))
            {
                final int tmp = lastHotbarSlot;
                lastHotbarSlot = -1;
                lastHotbarItem = null;
                return tmp;
            }
        }
        // Search through our inventory
        for (int slot = 36; slot >= 0; slot--)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(slot);
            if (!itemStack.isEmpty() && itemStack.getItem().equals(item))
            {
                return slot;
            }
        }
        return -1;
    }

    private Item getItemToWield()
    {
        // If the player's health (+absorption) falls below the "safe" amount, equip a totem
        final float health = PlayerUtil.getLocalPlayerHealth();
        if (health <= healthConfig.getValue())
        {
            return Items.TOTEM_OF_UNDYING;
        }
        // Check fall damage
        if (PlayerUtil.computeFallDamage(mc.player.fallDistance, 1.0f) + 0.5f > mc.player.getHealth())
        {
            return Items.TOTEM_OF_UNDYING;
        }
        if (lethalConfig.getValue())
        {
            final List<Entity> entities = Lists.newArrayList(mc.world.getEntities());
            for (Entity e : entities)
            {
                if (e == null || !e.isAlive() || !(e instanceof EndCrystalEntity crystal))
                {
                    continue;
                }
                if (mc.player.squaredDistanceTo(e) > 144.0)
                {
                    continue;
                }
                double potential = ExplosionUtil.getDamageTo(mc.player, crystal.getPos());
                if (health + 0.5 > potential)
                {
                    continue;
                }
                return Items.TOTEM_OF_UNDYING;
            }
        }
        // If offhand gap is enabled & the use key is pressed down, equip a golden apple.
        if (gappleConfig.getValue() && mc.options.useKey.isPressed() && (mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof TridentItem || mc.player.getMainHandStack().getItem() instanceof AxeItem))
        {
            return getGoldenAppleType();
        }
        return itemConfig.getValue().getItem();
    }

    private Item getGoldenAppleType()
    {
        if (crappleConfig.getValue()
                && mc.player.hasStatusEffect(StatusEffects.ABSORPTION)
                && InventoryUtil.hasItemInInventory(Items.GOLDEN_APPLE, true))
        {
            return Items.GOLDEN_APPLE;
        }
        return Items.ENCHANTED_GOLDEN_APPLE;
    }

    private enum OffhandItem
    {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.ENCHANTED_GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL);

        private final Item item;

        OffhandItem(Item item)
        {
            this.item = item;
        }

        public Item getItem()
        {
            return item;
        }
    }
}