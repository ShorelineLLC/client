package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.util.entity.PlayerUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CrystalEntityScanner extends AsyncWorldScanner
{
    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    private final List<CrystalData<?>> crystalEntities = new CopyOnWriteArrayList<>();

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

        float breakRange = autoCrystal.getBreakRange().getValue();
        double breakDist = getLocalEntity().getEyePos().squaredDistanceTo(crystal.getPos());
        if (breakDist > breakRange * breakRange)
        {
            return;
        }

        Vec3d localPos = getLocalEntity().getPos();
        Box localBox = getLocalEntity().getBoundingBox();
        float local = !PlayerUtil.isInSurvival(MinecraftClient.getInstance().player) ? 0.0f :
                CrystalUtil.getCrystalDamage(this, crystal.getPos(), localPos, localBox, autoCrystal.getIgnoreTerrain().getValue());

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

            float targetRange = autoCrystal.getTargetRange().getValue();
            double dist = getLocalEntity().squaredDistanceTo(entityPos);
            if (dist > targetRange * targetRange)
            {
                continue;
            }

            Box boundingBox = entity.getDimensions().getBoxAt(entityPos);
            float damage = CrystalUtil.getCrystalDamage(this,
                    crystal.getPos(),
                    entityPos,
                    boundingBox,
                    autoCrystal.getIgnoreTerrain().getValue());

            boolean antiSurround = AntiSurround.checkAntiSurroundQualifiers(this, crystal.getBlockPos());

            crystalEntities.add(new CrystalData<>(crystal, crystal.getPos(), entity, damage, local, antiSurround));
        }
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getBreakRange().getValue() + 1.0f);
    }
}
