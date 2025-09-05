package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
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

        float local = !PlayerUtil.isInSurvival(MinecraftClient.getInstance().player) ? 0.0f :
                CrystalUtil.getCrystalDamage(this, crystal.getPos(), getLocalEntity(), autoCrystal.getIgnoreTerrain().getValue());

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

            double entityDist = crystal.squaredDistanceTo(entity.getPos());
            if (entityDist > 144.0f)
            {
                continue;
            }

            float targetRange = autoCrystal.getTargetRange().getValue();
            double dist = getLocalEntity().squaredDistanceTo(entity.getPos());
            if (dist > targetRange * targetRange)
            {
                continue;
            }

            float damage = CrystalUtil.getCrystalDamage(this, crystal.getPos(), entity, autoCrystal.getIgnoreTerrain().getValue());
            boolean antiSurround = AntiSurround.checkAntiSurroundQualifiers(crystal.getBlockPos().down());

            crystalEntities.add(new CrystalData<>(crystal, entity, damage, local, antiSurround));
        }
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getBreakRange().getValue() + 1.0f);
    }
}
