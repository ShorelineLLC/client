package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.async.AsyncFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class CrystalManager extends AsyncFeature<CrystalData<BlockPos>>
{
    private final CrystalBaseScanner baseScanner = new CrystalBaseScanner();

    public CrystalManager()
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

        if (AutoCrystalModule.INSTANCE.isEnabled())
        {
            baseScanner.createCube(mc.world, mc.player.getBlockPos());

            runAsync(baseScanner::scanCrystalBases);
        }

        for (CrystalData<BlockPos> data : getResults())
        {
            mc.inGameHud.getChatHud().addMessage(Text.of(data.toString()));
        }
    }
}
