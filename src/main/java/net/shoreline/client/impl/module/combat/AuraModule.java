package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickPriorities;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.ItemSlot;
import net.shoreline.client.impl.inventory.SwapHandler;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.CombatModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.RotateMode;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;
import org.apache.commons.lang3.mutable.MutableDouble;

public class AuraModule extends CombatModule
{
    public static AuraModule INSTANCE;

    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setDefaultValue(4.0f).setMin(0.5f).setMax(6.0f).setFormat("m")
            .setDescription("The range to attack entities").build();
    Config<Boolean> hitDelay = new BooleanConfig.Builder("HitDelay")
            .setDescription("Hits only after attack delay has passed")
            .setDefaultValue(true).build();
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows you to use items while attacking")
            .setDefaultValue(true).build();
    Config<Boolean> requireWeapon = new BooleanConfig.Builder("RequireWeapon")
            .setDescription("Must be holding a weapon to attack")
            .setDefaultValue(false).build();
    Config<Boolean> swingConfig = new BooleanConfig.Builder("Swing")
            .setDescription("Swings the hand when attacking")
            .setDefaultValue(true).build();
    Config<RotateMode> rotateConfig = new EnumConfig.Builder<RotateMode>("Rotate")
            .setValues(RotateMode.values())
            .setDescription("Rotates to the entity before attacking")
            .setDefaultValue(RotateMode.OFF).build();

    Config<Boolean> targetPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Targets players").setDefaultValue(true).build();
    Config<Boolean> targetHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Targets hostiles").setDefaultValue(false).build();
    Config<Boolean> targetPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Targets passives").setDefaultValue(false).build();
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetPlayers, targetHostiles, targetPassives).build();

    Config<Boolean> autoSwap = new BooleanConfig.Builder("AutoSwap")
            .setDescription("Automatically swaps to a weapon before attacking")
            .setDefaultValue(false).build();
    Config<Boolean> silentSwap = new BooleanConfig.Builder("SilentSwap")
            .setVisibilityDependant(true)
            .setDescription("Swaps to a weapon silently")
            .setVisible(() -> autoSwap.getValue())
            .setDefaultValue(false).build();
    Config<Void> swapConfig = new ConfigGroup.Builder("Swap")
            .addAll(autoSwap, silentSwap).build();

    private final SwapHandler autoSwapHandler = new SwapHandler();
    private final Timer attackDelayTimer = new NanoTimer();

    @Getter
    private boolean running;
    private Entity auraTarget;
    private Box targetBox;

    private final Animation fadeAnim = new Animation(true, 300L);

    public AuraModule()
    {
        super("Aura", "Attacks nearby entities", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onDisable()
    {
        auraTarget = null;
        running = false;
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener(priority = TickPriorities.KILL_AURA)
    public void onClientRotation(ClientRotationEvent event)
    {
        running = false;
        if (checkNull() || event.isCanceled() || mc.player.isSpectator())
        {
            return;
        }

        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
           return;
        }

        if (AutoCrystalModule.INSTANCE.isRunning())
        {
            return;
        }

        auraTarget = getAuraTarget();
        if (auraTarget == null)
        {
            return;
        }

        float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), auraTarget.getEyePos());
        Rotation rotation = new Rotation(rotations[0], rotations[1]);
        if (rotateConfig.getValue() == RotateMode.NORMAL)
        {
            event.cancel();
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());
        } else if (rotateConfig.getValue() == RotateMode.SILENT)
        {
            Managers.ROTATION.setSilentRotation(rotation);
        }

        runAttack(auraTarget);

        if (rotateConfig.getValue() == RotateMode.SILENT)
        {
            Managers.ROTATION.resetSilentRotation();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (auraTarget != null)
        {
            targetBox = Interpolation.getEntityRenderBox(auraTarget, event.getTickDelta());
        } else
        {
            fadeAnim.setState(false);
        }

        if (targetBox == null)
        {
            return;
        }

        fadeAnim.setState(running);
        double scaledFactor = Easing.SMOOTH_STEP.ease(attackDelayTimer.getFactor());
        double animFactor = (MathHelper.clamp(0.5f + scaledFactor, 0.0f, 1.0f)) * fadeAnim.getFactor();

        int color = ThemeModule.INSTANCE.getPrimaryColor().getRGB();
        BoxRender.FILL.render(event.getMatrixStack(), targetBox, color, (float) animFactor);
    }

    private void runAttack(final Entity entity)
    {
        PlayerInventory playerInventory = mc.player.getInventory();

        ItemSlot weaponSlot = getAuraWeaponSlot();
        if (weaponSlot.getSlot() != -1)
        {
            if (silentSwap.getValue())
            {
                if (!Managers.INVENTORY.startSwap(weaponSlot.getSlot()))
                {
                    return;
                }

            } else if (autoSwap.getValue())
            {
                autoSwapHandler.handleSwaps();
                if (autoSwapHandler.canAutoSwap())
                {
                    Managers.INVENTORY.setSelectedSlot(weaponSlot.getSlot());
                }
            }
        }

        if (!requireWeapon.getValue() || Managers.INVENTORY.isHolding(weaponSlot.getItem(), Hand.MAIN_HAND))
        {
            running = true;
            ItemStack stack = weaponSlot.getItemStack() == null ? playerInventory.getMainHandStack() : weaponSlot.getItemStack();
            double attackDelay = 1.0 / getAttackSpeed(stack) * 20.0;

            if (attackDelayTimer.hasPassed(attackDelay * 50.0))
            {
                attackEntity(entity);
                attackDelayTimer.reset();
            }
        }

        if (silentSwap.getValue())
        {
            Managers.INVENTORY.endSwap();
        }
    }

    public void attackEntity(final Entity entity)
    {
        boolean sprinting = mc.player.isSprinting();
        if (sprinting)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        sendAttackPackets(entity, swingConfig.getValue());
        mc.player.resetLastAttackedTicks();

        if (sprinting)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }

    private double getAttackSpeed(ItemStack itemStack)
    {
        MutableDouble attackSpeed = new MutableDouble(
                mc.player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED));

        AttributeModifiersComponent attributeModifiers =
                itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null)
        {
            attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) ->
            {
                if (entry == EntityAttributes.ATTACK_SPEED)
                {
                    attackSpeed.add(modifier.value());
                }
            });
        }

        return attackSpeed.getValue();
    }

    private ItemSlot getAuraWeaponSlot()
    {
        ItemSlot swordSlot = InventoryUtil.getItemSlot((ItemStack itemStack) -> itemStack.getItem().getTranslationKey().contains("sword"));
        if (swordSlot.getSlot() != InventoryUtil.INVALID_SLOT)
        {
            return swordSlot;
        }

        ItemSlot axeSlot = InventoryUtil.getItemSlot((ItemStack itemStack) -> itemStack.getItem().getTranslationKey().contains("axe"));
        if (axeSlot.getSlot() != InventoryUtil.INVALID_SLOT)
        {
            return axeSlot;
        }

        return new ItemSlot(mc.player.getInventory(), InventoryUtil.getItemSlot(Items.TRIDENT));
    }

    private Entity getAuraTarget()
    {
        Entity target = null;
        for (Entity entity : mc.world.getEntities())
        {
            if (entity.equals(mc.player) || !entity.isAlive() || !canTargetToAttack(entity))
            {
                continue;
            }

            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > rangeConfig.getValue() * rangeConfig.getValue())
            {
                continue;
            }

            target = entity;
        }

        return target;
    }

    private boolean canTargetToAttack(Entity entity)
    {
        return entity instanceof PlayerEntity && targetPlayers.getValue()
                || EntityUtil.isHostile(entity) && targetHostiles.getValue()
                || EntityUtil.isPassive(entity) && targetPassives.getValue();
    }
}
