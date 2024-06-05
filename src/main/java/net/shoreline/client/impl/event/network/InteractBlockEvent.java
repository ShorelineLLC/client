package net.shoreline.client.impl.event.network;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class InteractBlockEvent extends Event
{
    //
    private final ClientPlayerEntity player;
    private final Hand hand;
    private final BlockHitResult hitResult;

    /**
     * @param player
     * @param hand
     * @param hitResult
     */
    public InteractBlockEvent(ClientPlayerEntity player, Hand hand,
                              BlockHitResult hitResult)
    {
        this.player = player;
        this.hand = hand;
        this.hitResult = hitResult;
    }

    public ClientPlayerEntity getPlayer()
    {
        return player;
    }

    public Hand getHand()
    {
        return hand;
    }

    public BlockHitResult getHitResult()
    {
        return hitResult;
    }
}
