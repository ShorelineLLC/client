package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.world.ExplosionUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xgraza, Shoreline
 * @since 1.0
 */
public final class AutoTotemModule extends ToggleModule
{
    Config<OffhandItem> itemConfig = register(new EnumConfig<>("Item", "The item to wield in your offhand", OffhandItem.TOTEM, OffhandItem.values()));
    Config<Float> healthConfig = register(new NumberConfig<>("Health", "The health required to fall below before swapping to a totem", 0.0f, 14.0f, 20.0f));
    Config<Boolean> gappleConfig = register(new BooleanConfig("OffhandGapple", "Equips a golden apple if holding down the item use button", true));
    Config<Boolean> lethalGappleConfig = register(new BooleanConfig("OffhandGapple-Lethal", "Checks lethal before offhand gapple", true, () -> false));
    Config<Boolean> crappleConfig = register(new BooleanConfig("Crapple", "Uses a normal golden apple if Absorption is present", true));
    Config<Boolean> lethalConfig = register(new BooleanConfig("Lethal", "Calculates lethal damage sources", false, () -> itemConfig.getValue() != OffhandItem.TOTEM));
    Config<Boolean> fastConfig = register(new BooleanConfig("FastSwap", "Swaps items to offhand", true));
    Config<Boolean> debugConfig = register(new BooleanConfig("Debug", "Debug on death", false));

    private int lastHotbarSlot, lastTotemCount;
    private Item lastHotbarItem;

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
        // This comment is funny, check the commit
        super.onDisable();
        lastHotbarSlot = -1;
        lastHotbarItem = null;
    }

    @EventListener
    public void onTick(final TickEvent event)
    {
        Item offhandItem = itemConfig.getValue().getItem();
        if (checkLethal() && offhandItem != Items.TOTEM_OF_UNDYING)
        {
            offhandItem = Items.TOTEM_OF_UNDYING;
        } else
        {
            // If offhand gap is enabled & the use key is pressed down, equip a golden apple.
            final Item mainHandItem = mc.player.getMainHandStack().getItem();
            if (gappleConfig.getValue() && mc.options.useKey.isPressed()
                    && (mainHandItem instanceof SwordItem
                        || mainHandItem instanceof TridentItem
                        || mainHandItem instanceof AxeItem)
                    && (!lethalConfig.getValue() || mc.player.getHealth() >= healthConfig.getValue()))
            {

                offhandItem = getGoldenAppleType();
            }
        }

        final int inventorySlot = getInventorySlot(offhandItem);
        if (inventorySlot == -1)
        {
            return;
        }
        if (inventorySlot < 9)
        {
            lastHotbarItem = offhandItem;
            lastHotbarSlot = inventorySlot;
        }
        swapToOffhand(inventorySlot);
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof HealthUpdateS2CPacket packet
                && packet.getHealth() <= 0.0f && debugConfig.getValue())
        {
            if (lastTotemCount <= 0)
            {
                return;
            }
            final Set<String> failureReasonsSet = getFailureReasons();
            if (failureReasonsSet.isEmpty())
            {
                sendModuleMessage("Failed to replace totem!");
            } else
            {
                sendModuleMessage("Failed to replace totem! Possible reasons: %s", String.join(", ", failureReasonsSet));
            }
        }
    }

    private Set<String> getFailureReasons()
    {
        final Set<String> failureReasonsSet = new LinkedHashSet<>();
        if (mc.player.currentScreenHandler.syncId != 0)
        {
            failureReasonsSet.add("Current screen handler is not the player inventory");
        }
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
        {
            failureReasonsSet.add("Totem was not placed in offhand on time");
        }
        return failureReasonsSet;
    }

    private void swapToOffhand(final int itemSlot)
    {
        if (fastConfig.getValue())
        {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    itemSlot < 9 ? itemSlot + 36 : itemSlot, 40, SlotActionType.SWAP, mc.player);
        }
        else
        {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    45, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        itemSlot < 9 ? itemSlot + 36 : itemSlot, 0, SlotActionType.PICKUP, mc.player);
            }
        }
        // Subtracting 1 from this number accounts for this totem that we are replacing
        lastTotemCount = Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING) - 1;
    }

    private int getInventorySlot(Item item)
    {
        // If we already have that item in our offhand, return
        if (mc.player.getOffHandStack().getItem() == item)
        {
            return -1;
        }
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

    private boolean checkLethal()
    {
        // If the player's health (+absorption) falls below the "safe" amount, equip a totem
        final float health = PlayerUtil.getLocalPlayerHealth();
        if (health <= healthConfig.getValue())
        {
            return true;
        }
        // Check fall damage
        if (PlayerUtil.computeFallDamage(mc.player.fallDistance, 1.0f) + 0.5f > mc.player.getHealth())
        {
            return true;
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
                return true;
            }
        }
        return false;
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