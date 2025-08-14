package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrystalEntityScanner extends AsyncWorldScanner
{
    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    private final List<CrystalData<?>> crystalEntites = new CopyOnWriteArrayList<>();

    public List<CrystalData<?>> scanCrystalEntities()
    {
        crystalEntites.clear();

        for (EntityState state : getEntities())
        {
            if (!state.getEntity().isAlive() || !(state.getEntity() instanceof EndCrystalEntity))
            {
                continue;
            }

            visitEndCrystal(state);
        }

        return crystalEntites;
    }

    private void visitEndCrystal(EntityState entityState)
    {
        if (entityState.getAge() < autoCrystal.getTicksExisted().getValue())
        {
            return;
        }

        float breakRange = autoCrystal.getBreakRange().getValue();
        double breakDist = getLocalEntity().getEyePos().squaredDistanceTo(entityState.getPos());
        if (breakDist > breakRange * breakRange)
        {
            return;
        }

        float local = CrystalUtil.getCrystalDamage(this, entityState.getPos(), getLocalEntity(), autoCrystal.getIgnoreTerrain().getValue());

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

            double entityDist = entityState.squaredDistanceTo(entity.getPos());
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

            float damage = CrystalUtil.getCrystalDamage(this, entityState.getPos(), entity, autoCrystal.getIgnoreTerrain().getValue());

            crystalEntites.add(new CrystalData<>(entityState, entity, damage, local));
        }
    }

    @Override
    protected void visit(BlockPos pos, BlockState state) {}

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getBreakRange().getValue());
    }
}
