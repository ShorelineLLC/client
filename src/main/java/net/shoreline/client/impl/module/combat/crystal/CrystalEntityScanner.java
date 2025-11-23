package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.explosion.ExplosionScanner;
import net.shoreline.client.util.entity.PlayerUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CrystalEntityScanner extends ExplosionScanner
{
    private static final EntityDimensions ITEM_DIMENSIONS = EntityDimensions.fixed(0.25f, 0.25f);

    protected final AutoCrystalModule autoCrystal;
    protected final CrystalDataFactory factory;

    private final List<CrystalData<?>> crystalEntities = new CopyOnWriteArrayList<>();

    protected CrystalEntityScanner(AutoCrystalModule autoCrystal)
    {
        super(12.0f);
        this.autoCrystal = autoCrystal;
        this.factory = new CrystalDataFactory(this);
    }

    public List<CrystalData<?>> scanCrystalEntities()
    {
        crystalEntities.clear();

        for (EntityState state : getEntities())
        {
            if (!state.isAlive() || !(state.getEntity() instanceof EndCrystalEntity))
            {
                continue;
            }

            visitEndCrystal(state);
        }

        return crystalEntities;
    }

    private void visitEndCrystal(EntityState crystal)
    {
        if (crystal.getAge() < autoCrystal.getTicksExisted().getValue())
        {
            return;
        }

        double breakDist = getLocalEntity().getEyePos().squaredDistanceTo(crystal.getPos());
        if (breakDist > MathHelper.square(autoCrystal.getBreakRange().getValue()))
        {
            return;
        }

        Vec3d localPos = getLocalEntity().getPos();
        Box localBox = getLocalEntity().getBoundingBox();
        float local = !PlayerUtil.isInSurvival(MinecraftClient.getInstance().player) ? 0.0f :
                getExplosionDamage(crystal.getPos(), localPos, localBox, autoCrystal.getIgnoreTerrain().getValue());

        boolean willKillPlayer = getLocalEntity().getTotalHealth() - local < 0.5f;
        if (local > autoCrystal.getMaxSelfDamage().getValue() || willKillPlayer)
        {
            return;
        }

        for (EntityState entity : getEntities())
        {
            if (!(entity.getEntity() instanceof LivingEntity) || !autoCrystal.canTargetEntity(entity.getEntity()))
            {
                continue;
            }

            int ticks = autoCrystal.getExtrapolateTicks().getValue();
            Vec3d entityPos = ticks <= 0 ? entity.getPos() : MovementExtrapolation.extrapolatePosition(this,
                    entity.getVelocity(),
                    entity.getBoundingBox(),
                    entity.getEntity(),
                    ticks);

            double entityDist = crystal.squaredDistanceTo(entityPos);
            if (entityDist > 144.0f)
            {
                continue;
            }

            double dist = getLocalEntity().squaredDistanceTo(entityPos);
            if (dist > MathHelper.square(autoCrystal.getTargetRange().getValue()))
            {
                continue;
            }

            Box boundingBox = entity.getDimensions().getBoxAt(entityPos);
            float damage = getExplosionDamage(crystal.getPos(),
                    entityPos,
                    boundingBox,
                    autoCrystal.getIgnoreTerrain().getValue());

            crystalEntities.add(factory.createData(crystal, crystal.getPos(), entity, damage, local));
        }
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getBreakRange().getValue() + 1.0f);
    }

    public boolean isAntiSurroundPos(BlockPos blockPos)
    {
        if (!AutoMineModule.INSTANCE.isEnabled() || !SpeedMineModule.INSTANCE.isEnabled())
        {
            return false;
        }

        PlayerEntity target = Managers.TARGETING.getTarget();
        if (target == null)
        {
            return false;
        }

        MiningData currentMine = SpeedMineModule.INSTANCE.getMainMiningBlock();
        if (currentMine == null || SpeedMineModule.INSTANCE.isManualMining())
        {
            return false;
        }

        if (currentMine.isDoneMining())
        {
            BlockPos minePos = currentMine.getBlockPos();
            EntityState state = getEntityById(target.getId());

            float baseDamage = getExplosionDamage(minePos.toBottomCenterPos(),
                    state.getPos(),
                    state.getBoundingBox(),
                    AutoCrystalModule.INSTANCE.getIgnoreTerrain().getValue(),
                    Set.of(minePos));

            baseDamage *= 0.11f; // We have to assume armor here...
            if (baseDamage < AutoCrystalModule.INSTANCE.getMinDamage().getValue())
            {
                return false;
            }

            for (EntityState entityState : getOtherEntities(null, new Box(minePos)))
            {
                if (entityState.getEntity() instanceof ItemEntity)
                {
                    float damage = getExplosionDamage(blockPos.toBottomCenterPos(),
                            entityState.getPos(),
                            entityState.getBoundingBox(),
                            false,
                            Set.of(minePos));

                    if (damage >= 5.0f)
                    {
                        return true;
                    }
                }
            }
        }

        else if (currentMine.isAlmostDone(autoCrystal.getPrePlace().getValue()))
        {
            BlockPos minePos = currentMine.getBlockPos();
            Vec3d simPos = minePos.toBottomCenterPos();
            float damage = getExplosionDamage(blockPos.toBottomCenterPos(),
                    simPos,
                    ITEM_DIMENSIONS.getBoxAt(simPos),
                    false,
                    Set.of(minePos));

            return damage >= 5.0f;
        }

        return false;
    }
}
