package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.combat.crystal.CrystalData;
import net.shoreline.client.impl.module.combat.util.TickPriorities;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public class AutoCrystalModule extends ListeningToggleable
{
    public static AutoCrystalModule INSTANCE;

    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while interacting")
            .setDefaultValue(true).build();

    Config<Float> targetRange = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(15.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to target entities").build();
    Config<Boolean> targetPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Targets players").setDefaultValue(true).build();
    Config<Boolean> targetHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Targets hostiles").setDefaultValue(false).build();
    Config<Boolean> targetPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Targets passives").setDefaultValue(false).build();
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetRange, targetPlayers, targetHostiles, targetPassives).build();

    Config<Float> placeRange = new NumberConfig.Builder<Float>("PlaceRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to place crystals").build();
    Config<Integer> placeDelay = new NumberConfig.Builder<Integer>("PlaceDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between placing crystals").build();
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only places crystals on visible faces")
            .setDefaultValue(false).build();
    Config<Boolean> protocolPlace = new BooleanConfig.Builder("Protocol")
            .setDescription("Prevents placements in 1x1 areas")
            .setDefaultValue(false).build();
    Config<Void> placeConfig = new ConfigGroup.Builder("Place")
            .addAll(placeRange, placeDelay, strictDirection, protocolPlace).build();

    Config<Float> breakRange = new NumberConfig.Builder<Float>("BreakRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to break crystals").build();
    Config<Integer> breakDelay = new NumberConfig.Builder<Integer>("BreakDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between breaking crystals").build();
    Config<Integer> ticksExisted = new NumberConfig.Builder<Integer>("TicksExisted")
            .setMin(0).setMax(10).setDefaultValue(0)
            .setDescription("The minimum ticks existed before breaking crystals").build();
    Config<Void> breakConfig = new ConfigGroup.Builder("Break")
            .addAll(breakRange, breakDelay, ticksExisted).build();

    Config<Boolean> targetItems = new BooleanConfig.Builder("TargetItems")
            .setDescription("Targets dropped items blocking placements")
            .setDefaultValue(false).build();
    Config<Boolean> forcePlace = new BooleanConfig.Builder("ForcePlace")
            .setDescription("Attempts to force crystal placements in blocked positions")
            .setDefaultValue(false).build();
    Config<Void> antiSurroundConfig = new ConfigGroup.Builder("AntiSurround")
            .addAll(targetItems, forcePlace).build();

    Config<Float> minDamage = new NumberConfig.Builder<Float>("MinDamage")
            .setMin(1.0f).setMax(10.0f).setDefaultValue(4.0f)
            .setDescription("The minimum damage to consider crystals").build();
    Config<Float> maxSelfDamage = new NumberConfig.Builder<Float>("MaxSelfDamage")
            .setMin(1.0f).setMax(20.0f).setDefaultValue(12.0f)
            .setDescription("The maximum damage a crystal can do to the player").build();
    Config<Boolean> overrideConfig = new BooleanConfig.Builder("Override")
            .setDescription("Allows overriding minimum damage (e.g. allows crystal spam)")
            .setDefaultValue(true).build();
    Config<Integer> minArmorDamage = new NumberConfig.Builder<Integer>("MinArmorDamage")
            .setMin(0).setMax(100).setDefaultValue(5).setFormat("%")
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("The minimum armor damage to consider spamming crystals").build();
    Config<Float> damageMultiplier = new NumberConfig.Builder<Float>("DamageMultiplier")
            .setMin(1.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("Place if we can kill target in this many crystals").build();
    Config<Boolean> ignoreTerrain = new BooleanConfig.Builder("IgnoreTerrain")
            .setDescription("Ignores explodable terrain during damage calculations")
            .setDefaultValue(false).build();
    Config<Void> damageConfig = new ConfigGroup.Builder("Damage")
            .addAll(minDamage, maxSelfDamage, overrideConfig, minArmorDamage,
                    damageMultiplier, ignoreTerrain).build();

    Config<Boolean> shouldRotate = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates before placing crystals")
            .setDefaultValue(false).build();
    Config<Boolean> rotatePacket = new BooleanConfig.Builder("SilentRotate")
            .setDescription("Rotates without looking at the crystal")
            .setVisible(() -> shouldRotate.getValue())
            .setDefaultValue(false).build();
    Config<Float> yawLimit = new NumberConfig.Builder<Float>("FOV")
            .setMin(1.0f).setMax(180.0f).setDefaultValue(180.0f).setFormat("deg")
            .setDescription("The field of view for attacking").build();
    Config<Void> rotateConfig = new ConfigGroup.Builder("Rotate")
            .addAll(rotatePacket, yawLimit).build();

    Config<Boolean> autoSwap = new BooleanConfig.Builder("AutoSwap")
            .setDescription("Automatically swaps to crystals before placing")
            .setDefaultValue(false).build();
    Config<Boolean> silentSwap = new BooleanConfig.Builder("SilentSwap")
            .setDescription("Silently swaps to crystals before placing")
            .setVisible(() -> autoSwap.getValue())
            .setDefaultValue(false).build();
    Config<Boolean> antiWeakness = new BooleanConfig.Builder("AntiWeakness")
            .setDescription("Swaps to sword before attacking crystals")
            .setVisible(() -> autoSwap.getValue() && silentSwap.getValue())
            .setDefaultValue(false).build();
    Config<Void> swapConfig = new ConfigGroup.Builder("Swap")
            .addAll(autoSwap, silentSwap, antiWeakness).build();

    private CrystalData<BlockPos> currentPlace;

    private final ConcurrentMap<BlockPos, Animation> fadeAnimations = new ConcurrentHashMap<>();

    public AutoCrystalModule()
    {
        super("AutoCrystal", new String[] {"CrystalAura"}, "Best CA on the market", GuiCategory.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onDisable()
    {
        currentPlace = null;
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener(priority = TickPriorities.AUTO_CRYSTAL)
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
            return;
        }

        List<CrystalData<BlockPos>> latestCrystalBases = Managers.CRYSTAL.getBaseResults();
        List<CrystalData<EntityState>> latestCrystalEntities = Managers.CRYSTAL.getEntityResults();

        currentPlace = getPlacement(latestCrystalBases);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (currentPlace != null)
        {
            fadeAnimations.put(currentPlace.getCrystalData(), new Animation(true, 250));
        }

        for (Map.Entry<BlockPos, Animation> entry : fadeAnimations.entrySet())
        {
            BlockPos placeData = entry.getKey();
            Animation anim = entry.getValue();

            if (anim.getFactor() <= 0.01)
            {
                fadeAnimations.remove(placeData);
                continue;
            }

            anim.setState(false);
            BoxRender.FILL.render(event.getMatrixStack(),
                    placeData, ThemeModule.INSTANCE.getPrimaryColor().getRGB(),
                    (float) Easing.SMOOTH_STEP.ease(anim.getFactor()));
        }
    }

    private CrystalData<BlockPos> getPlacement(List<CrystalData<BlockPos>> crystalBases)
    {
        if (crystalBases.isEmpty())
        {
            return null;
        }

        CrystalData<BlockPos> bestPlace = null;
        float bestDamage = 0.0f;

        for (CrystalData<BlockPos> base : crystalBases)
        {
            LivingEntity entity = (LivingEntity) base.getTarget().getEntity();
            float baseDamage = (float) base.getDamageToTarget();

            if (entity.isDead() || baseDamage < bestDamage)
            {
                continue;
            }

            float damage = ExplosionUtil.getAppliedDamageToEntity(entity, baseDamage);
            if (damage > bestDamage)
            {
                bestDamage = damage;
                bestPlace = base;
            }
        }

        if (bestDamage < minDamage.getValue())
        {
            return null;
        }

        return bestPlace;
    }

    public boolean canTargetEntity(Entity entity)
    {
        return entity instanceof PlayerEntity && targetPlayers.getValue()
                || EntityUtil.isHostile(entity) && targetHostiles.getValue()
                || EntityUtil.isPassive(entity) && targetPassives.getValue();
    }
}
