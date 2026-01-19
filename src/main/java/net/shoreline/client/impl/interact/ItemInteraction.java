package net.shoreline.client.impl.interact;

import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.rotation.Rotation;

import java.util.concurrent.atomic.AtomicInteger;

public class ItemInteraction extends Interaction<Item>
{
    public static final AtomicInteger GLOBAL_COUNT = new AtomicInteger();

    private final Rotation rotation;

    public ItemInteraction(Item interact, Hand hand, Rotation rotation, boolean clientInteract)
    {
        super("ItemUseInteraction", interact, hand, clientInteract);
        this.rotation = rotation;
    }

    public ItemInteraction(Item interact, Hand hand, boolean clientInteract)
    {
        super("ItemUseInteraction", interact, hand, clientInteract);
        this.rotation = Managers.ROTATION.hasClientRotation() ? Managers.ROTATION.getClientRotation() : new Rotation(mc.player);
    }

    @Override
    public ActionResult applyInteraction()
    {
        if (!clientInteract || mc.isOnThread())
        {
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(hand, id, rotation.getYaw(), rotation.getPitch()));
            return ActionResult.SUCCESS;
        } else
        {
            Rotation playerRotation = new Rotation(mc.player);
            rotation.applyToPlayer();
            ActionResult result = mc.interactionManager.interactItem(mc.player, hand);
            playerRotation.applyToPlayer();
            return result;
        }
    }
}
