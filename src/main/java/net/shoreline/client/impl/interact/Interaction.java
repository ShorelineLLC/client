package net.shoreline.client.impl.interact;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.shoreline.client.impl.network.NetworkHandler;

@Getter
@Setter
public abstract class Interaction<T> extends NetworkHandler
{
    protected T interact;
    protected Hand hand;
    protected boolean clientInteract;

    protected InteractStatus status = InteractStatus.UNCONFIRMED;

    public Interaction(String name, T interact, Hand hand, boolean clientInteract)
    {
        super(name);
        this.interact = interact;
        this.hand = hand;
        this.clientInteract = clientInteract;
    }

    public Interaction(String name, Hand hand, boolean clientInteract)
    {
        super(name);
        this.hand = hand;
        this.clientInteract = clientInteract;
    }

    public abstract ActionResult applyInteraction();
}
