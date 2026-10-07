package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;
import net.shoreline.client.impl.world.explosion.ExplosionScanner;
import net.shoreline.client.util.entity.PlayerUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CrystalEntityScanner extends ExplosionScanner
{
    protected final AutoCrystalModule autoCrystal;
    protected final CrystalDataFactory factory;

    private final List<CrystalData<?>> crystalEntities = new CopyOnWriteArrayList<>();

    protected CrystalEntityScanner(AutoCrystalModule autoCrystal)
    {
        super(12.0f);
        this.autoCrystal = autoCrystal;
        this.factory = new CrystalDataFactory(autoCrystal);
    }

    public List<CrystalData<?>> scanCrystalEntities()
    {
        crystalEntities.clear();

        for (EntityState state : getEntities())
        {
            if (!state.isAlive() || state.getEntityType() != EntityType.END_CRYSTAL)
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

        for (LivingEntityState entity : getLivingEntities())
        {
            if (Managers.SOCIAL.isFriend(entity.getName())
                    || entity.getTotalArmor() <= 0 && !autoCrystal.getTargetNakeds().getValue()
                    || !autoCrystal.isValid(entity.getEntityType()))
            {
                continue;
            }

            int ticks = autoCrystal.getExtrapolateTicks().getValue();
            Vec3d entityPos = ticks <= 0 ? entity.getPos() : MovementExtrapolation.extrapolatePosition(this,
                    box -> getBlockCollisions(entity, box),
                    entity.getVelocity(),
                    entity.getBoundingBox(),
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
}