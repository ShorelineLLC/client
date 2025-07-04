package net.shoreline.client.impl.player.interact;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.AnticheatModule;

public class InteractManager extends GenericFeature
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    private final Int2IntMap placedEntityIds = new Int2IntOpenHashMap();

    public InteractManager()
    {
        super("Interactions");
    }

    public void placeBlock()
    {

    }

    private ActionResult placeBlockInternal(Interaction interaction)
    {
        Vec3d interactionVec = getVecFromDirection(interaction.getDirection());
        final BlockHitResult result = new BlockHitResult(interactionVec,
                interaction.getDirection(), interaction.getPos(), false);
        if (interaction.isPacketPlace())
        {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(interaction.getHand(), result, id));
            return ActionResult.SUCCESS;
        } else
        {
            return mc.interactionManager.interactBlock(mc.player, interaction.getHand(), result);
        }
    }

    private Vec3d getVecFromDirection(Direction direction)
    {
        return null;
    }
}
