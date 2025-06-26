package net.shoreline.client.impl.player.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwapData
{
    private boolean swapped;
    private SilentSwapType swapType;

    private int slotFrom, slotTo;
}
