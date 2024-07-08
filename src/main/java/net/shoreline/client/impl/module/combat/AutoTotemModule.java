package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.player.PlayerUtil;
import net.shoreline.client.util.world.ExplosionUtil;
import net.shoreline.eventbus.event.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xgraza, Shoreline
 * @since 1.0
 */
public final class AutoTotemModule extends ToggleModule
{
    private static AutoTotemModule INSTANCE;

    Config<OffhandItem> itemConfig = register(new EnumConfig<>("Item", "The item to wield in your offhand", OffhandItem.TOTEM, OffhandItem.values()));
    Config<Float> healthConfig = register(new NumberConfig<>("Health", "The health required to fall below before swapping to a totem", 0.0f, 14.0f, 20.0f));
    Config<Boolean> gappleConfig = register(new BooleanConfig("OffhandGapple", "Equips a golden apple if holding down the item use button", true));
    Config<Boolean> lethalGappleConfig = register(new BooleanConfig("OffhandGapple-Lethal", "Checks lethal before swapping offhand to gapple", true, () -> false));
    Config<Boolean> crappleConfig = register(new BooleanConfig("Crapple", "Uses a normal golden apple if Absorption is present", true));
    Config<Boolean> lethalConfig = register(new BooleanConfig("Lethal", "Calculates lethal damage sources", false, () -> itemConfig.getValue() != OffhandItem.TOTEM));
    Config<Boolean> fastConfig = register(new BooleanConfig("FastSwap", "Swaps items to offhand", true));
    Config<Boolean> alternativeConfig = register(new BooleanConfig("Alternative", "Replaces totem using the swap packet", false));
    Config<Boolean> debugConfig = register(new BooleanConfig("Debug", "Debug on death", false));

    private int lastHotbarSlot, lastTotemCount;
    private Item lastHotbarItem;
    private Item offhandItem;
    private boolean replacing;
    private long replaceTime;

    public AutoTotemModule()
    {
        super("AutoTotem", "Automatically replenishes the totem in your offhand", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static AutoTotemModule getInstance()
    {
        return INSTANCE;
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
        offhandItem = null;
    }

    @EventListener
    public void onTick(final TickEvent event)
    {
        if (mc.player == null || event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        offhandItem = itemConfig.getValue().getItem();
        if (offhandItem != Items.TOTEM_OF_UNDYING && checkLethal())
        {
            offhandItem = Items.TOTEM_OF_UNDYING;
        }
        else
        {
            // If offhand gap is enabled & the use key is pressed down, equip a golden apple.
            final Item mainHandItem = mc.player.getMainHandStack().getItem();
            if (gappleConfig.getValue() && mc.options.useKey.isPressed()
                    && (mainHandItem instanceof SwordItem
                    || mainHandItem instanceof TridentItem
                    || mainHandItem instanceof AxeItem)
                    && (!lethalGappleConfig.getValue() || PlayerUtil.getLocalPlayerHealth() >= healthConfig.getValue()))
            {

                offhandItem = getGoldenAppleType();
            }
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
                if (alternativeConfig.getValue())
                {
                    mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
                    replacing = false;
                }
                else
                {
                    if (mc.player.currentScreenHandler.getCursorStack().getItem() != offhandItem)
                    {
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                    }
                    if (mc.player.currentScreenHandler.getCursorStack().getItem() == offhandItem)
                    {
                        mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
                        lastTotemCount = Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING) - 1;
                    }
                    replacing = false;
                    if (!mc.player.currentScreenHandler.getCursorStack().isEmpty() && mc.player.getOffHandStack().getItem() == offhandItem)
                    {
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        return;
                    }
                }
            }
            n--;
        }
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
                long serverLatency = System.currentTimeMillis() - replaceTime;
                sendModuleMessage("Failed to replace totem in %sms!", serverLatency);
            }
            else
            {
                sendModuleMessage("Failed to replace totem! Possible reasons: %s", String.join(", ", failureReasonsSet));
            }
        }
        // Server should only send this when we pop a totem
        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet
                && packet.getSlot() == 45 && offhandItem == Items.TOTEM_OF_UNDYING)
        {
            if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING || !packet.getStack().isEmpty())
            {
                return;
            }
            replaceTime = System.currentTimeMillis();
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

    public boolean isReplacing()
    {
        return replacing;
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