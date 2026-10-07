package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.world.EntityState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrystalCalcManager extends AsyncFeature<Collection<CrystalData<?>>>
{
    private final AutoCrystalModule autoCrystalModule;
    private final CrystalBaseScanner baseScanner;

    public CrystalCalcManager(AutoCrystalModule autoCrystalModule)
    {
        super("End Crystals", new ArrayList<>());
        this.autoCrystalModule = autoCrystalModule;
        this.baseScanner = new CrystalBaseScanner(autoCrystalModule);
    }

    public void runCalc()
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

    public List<CrystalData<BlockPos>> getBasePlacements()
    {
        return getBaseResults().stream()
                .filter(d -> autoCrystalModule.canUseOnBlock(d.getValue()))
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
