package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.world.EntityState;

import java.util.ArrayList;
import java.util.List;

public class CrystalCalcManager extends AsyncFeature<CrystalData<?>>
{
    private final CrystalBaseScanner baseScanner;

    public CrystalCalcManager(AutoCrystalModule autoCrystalModule)
    {
        super("End Crystals");
        this.baseScanner = new CrystalBaseScanner(autoCrystalModule);
    }

    public void runCalcs()
    {
        if (currentResult == null || currentResult.isDone())
        {
            baseScanner.createWorldLookup(mc.world, mc.player, true);
            runAsync(() ->
            {
                List<CrystalData<?>> crystalData = new ArrayList<>();
                crystalData.addAll(baseScanner.scanCrystalBases());
                crystalData.addAll(baseScanner.scanCrystalEntities());
                return crystalData;
            });
        }
    }

    @SuppressWarnings("unchecked cast")
    public List<CrystalData<BlockPos>> getBaseResults()
    {
        return getResults().stream()
                .filter(d -> d.getValue() instanceof BlockPos)
                .map(d -> (CrystalData<BlockPos>) d)
                .toList();
    }

    @SuppressWarnings("unchecked cast")
    public List<CrystalData<EntityState>> getEntityResults()
    {
        return getResults().stream()
                .filter(d -> d.getValue() instanceof EntityState)
                .map(d -> (CrystalData<EntityState>) d)
                .toList();
    }
}
