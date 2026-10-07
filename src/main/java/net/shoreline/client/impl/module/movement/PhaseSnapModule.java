package net.shoreline.client.impl.module.movement;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.network.PlayerMoveEvent;
import net.shoreline.client.impl.module.combat.util.PhaseUtil;
import net.shoreline.client.impl.module.impl.MovementModule;
import net.shoreline.client.impl.module.movement.speed.BaseSpeedFeature;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

// this shit is so useless
public class PhaseSnapModule extends MovementModule
{
    private final MovementService movementService;

    public PhaseSnapModule()
    {
        super("PhaseSnap", "Snaps you into a 4 block phase", GuiCategory.MOVEMENT);
        this.movementService = new MovementService();
    }

    @Override
    public void onEnable()
    {
        if (checkNull())
        {
            this.disable();
            return;
        }

        movementService.setGoal(null);
        if (!PhaseUtil.isInsideBlock(mc.player))
        {
            sendClientChatMessage("You're not phased!");
            this.disable();
            return;
        }

        super.onEnable();
    }

    @Override
    public void onDisable()
    {
        movementService.setGoal(null);
    }

    @EventListener
    public void onMove(PlayerMoveEvent event)
    {
        Vec3d goal = findGoal(mc.player.getBoundingBox());
        if (goal == null)
        {
            return;
        }

        movementService.setGoal(goal);
        if (movementService.checkGoal())
        {
            this.disable();
            return;
        }

        event.cancel();
        event.setMovement(movementService.onMoveUpdate(this, event.getMovement()));
    }

    public Vec3d findGoal(Box box)
    {
        List<BlockPos> phasedBlocks = PhaseUtil.intersectingBlocks(box);
        if (phasedBlocks.isEmpty() || phasedBlocks.size() > 6)
        {
            return null;
        }

        Box phasedBox = BlockUtil.getBoundingBox(phasedBlocks, 1);
        double centerX = (phasedBox.minX + phasedBox.maxX) / 2;
        double centerZ = (phasedBox.minZ + phasedBox.maxZ) / 2;
        return new Vec3d(centerX, mc.player.getY(), centerZ);
    }

    private static final class MovementService extends BaseSpeedFeature<PhaseSnapModule>
    {
        @Setter
        @Getter
        private Vec3d goal;

        public MovementService()
        {
            super("PhaseSnap-MovementHandler");
        }

        // TODO
        @Override
        public Vec3d onMoveUpdate(PhaseSnapModule module, Vec3d currentMove)
        {
            Vec3d goal = getGoal();
            if (goal == null)
            {
                return currentMove;
            }

            Vec3d offset = goal.subtract(mc.player.getPos());
            double speed = getBaseSpeed();
            return currentMove;
        }

        public boolean checkGoal()
        {
            Vec3d goal = getGoal();
            if (goal == null)
            {
                return true;
            }

            Vec3d offset = goal.subtract(mc.player.getPos());
            double dist = Math.hypot(offset.getX(), offset.getZ());
            return dist < 1.0E-4;
        }
    }
}
