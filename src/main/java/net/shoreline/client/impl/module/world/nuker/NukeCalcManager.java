package net.shoreline.client.impl.module.world.nuker;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.module.world.NukerModule;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public class NukeCalcManager extends AsyncFeature<Collection<BlockPos>>
{
    private final NukeScanner scanner;

    public NukeCalcManager(NukeScanner scanner)
    {
        super("Nuclear bombs");
        this.scanner = scanner;
    }

    public void runCalc(NukerModule.ScanMode mode)
    {
        mode.scan(scanner, mc.world);
        runAsync(() -> new ConcurrentSkipListSet<>(scanner.scanAndGet()));
    }
}
