package net.shoreline.client.impl.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class SwapData
{
    private final HotbarCache preHotbar;
    private final int slotFrom, slotTo;

    @Getter
    @Setter
    public static class Mutable
    {
        private boolean swapped;
        private int slotFrom, slotTo = -1;

        public void reset()
        {
            swapped = false;
            slotFrom = -1;
            slotTo = -1;
        }
    }
}
