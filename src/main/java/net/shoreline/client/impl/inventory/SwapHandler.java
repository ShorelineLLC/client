package net.shoreline.client.impl.inventory;

import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.impl.module.combat.AutoTotemModule;
import net.shoreline.client.util.input.InputUtil;

public class SwapHandler extends GenericFeature
{
    private final Timer lastSwapTime = new NanoTimer();

    public SwapHandler()
    {
        super("AutoSwap");
    }

    public void handleSwaps()
    {
        if (!mc.options.useKey.isPressed() && !mc.options.attackKey.isPressed() && !InputUtil.isInputtingHotbar())
        {
            return;
        }

        lastSwapTime.reset();
    }

    public boolean canAutoSwap()
    {
        return lastSwapTime.hasPassed(500) && !AutoTotemModule.INSTANCE.isTotemInMainHand();
    }
}
