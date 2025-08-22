package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

public class CrystalCalcManager extends AsyncFeature<CrystalData<?>>
{
    private final CrystalBaseScanner baseScanner = new CrystalBaseScanner();

    public CrystalCalcManager()
    {
        super("End Crystals");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        if (!AutoCrystalModule.INSTANCE.shouldRunCalcs())
        {
            currentResult = null;
            return;
        }

        if (currentResult == null || currentResult.isDone())
        {
            baseScanner.createSphere(mc.world, EntityUtil.getRoundedBlockPos(mc.player));
            baseScanner.createEntityLookup(mc.world, mc.player.getEyePos());

            runAsync(() ->
            {
                List<CrystalData<?>> crystalData = baseScanner.scanCrystalBases();
                crystalData.addAll(baseScanner.scanCrystalEntities());
                return crystalData;
            });
        }
    }

    @SuppressWarnings("unchecked cast")
    public List<CrystalData<BlockPos>> getBaseResults()
    {
        return getResults().stream()
                .filter(d -> d.getCrystalData() instanceof BlockPos)
                .map(d -> (CrystalData<BlockPos>) d)
                .toList();
    }

    @SuppressWarnings("unchecked cast")
    public List<CrystalData<EntityState>> getEntityResults()
    {
        return getResults().stream()
                .filter(d -> d.getCrystalData() instanceof EntityState)
                .map(d -> (CrystalData<EntityState>) d)
                .toList();
    }
}
