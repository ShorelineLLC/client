package net.shoreline.client.impl.manager.combat.hole;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.render.HoleESPModule;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author linus
 * @since 1.0
 */
public class HoleManager implements Globals
{
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private Future<Set<Hole>> result;
    //
    private Set<Hole> holes = new ConcurrentSet<>();

    public HoleManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    /**
     * @param event
     */
    @EventListener
    public void onTickEvent(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        HoleTask runnable = new HoleTask(getSphere(mc.player.getPos()));
        result = executor.submit(runnable);
    }

    public List<BlockPos> getSphere(Vec3d start)
    {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(Math.max(5.0, HoleESPModule.getInstance().getRange()));
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (start.getX() + x),
                            (int) (start.getY() + y), (int) (start.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    //
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    /**
     * @param pos
     * @return
     */
    public Hole checkHole(BlockPos pos)
    {
        if (pos.getY() == mc.world.getBottomY() && !BlastResistantBlocks.isUnbreakable(pos))
        {
            return new Hole(pos, HoleType.VOID);
        }
        int resistant = 0;
        int unbreakable = 0;
        if (BlockUtil.isBlockAccessible(pos))
        {
            BlockPos pos1 = pos.add(-1, 0, 0);
            BlockPos pos2 = pos.add(0, 0, -1);
            if (BlastResistantBlocks.isBlastResistant(pos1))
            {
                resistant++;
            }
            else if (BlastResistantBlocks.isUnbreakable(pos1))
            {
                unbreakable++;
            }
            if (BlastResistantBlocks.isBlastResistant(pos2))
            {
                resistant++;
            }
            else if (BlastResistantBlocks.isUnbreakable(pos2))
            {
                unbreakable++;
            }
            if (resistant + unbreakable < 2)
            {
                return null;
            }
            BlockPos pos3 = pos.add(0, 0, 1);
            BlockPos pos4 = pos.add(1, 0, 0);
            boolean air3 = mc.world.isAir(pos3);
            boolean air4 = mc.world.isAir(pos4);
            // Quad hole, player can stand in the middle of four blocks
            // to prevent placements on these blocks
            if (air3 && air4)
            {
                BlockPos pos5 = pos.add(1, 0, 1);
                if (!mc.world.isAir(pos5))
                {
                    return null;
                }
                BlockPos[] quad = new BlockPos[]
                        {
                                pos.add(-1, 0, 1),
                                pos.add(0, 0, 2),
                                pos.add(1, 0, 2),
                                pos.add(2, 0, 1),
                                pos.add(2, 0, 0),
                                pos.add(1, 0, -1)
                        };
                for (BlockPos p : quad)
                {
                    if (BlastResistantBlocks.isBlastResistant(p))
                    {
                        resistant++;
                    }
                    else if (BlastResistantBlocks.isUnbreakable(p))
                    {
                        unbreakable++;
                    }
                }
                if (resistant != 8 && unbreakable != 8 && resistant + unbreakable != 8)
                {
                    return null;
                }
                Hole quadHole = new Hole(pos, resistant == 8 ? HoleType.OBSIDIAN :
                        unbreakable == 8 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK,
                        pos1, pos2, pos3, pos4, pos5);
                quadHole.addHoleOffsets(quad);
                return quadHole;
            }
            // Double Z hole, player can stand in the middle of the blocks
            // to prevent placements on these blocks
            else if (air3 && BlockUtil.isBlockAccessible(pos3))
            {
                BlockPos[] doubleZ = new BlockPos[]
                        {
                                pos.add(-1, 0, 1),
                                pos.add(0, 0, 2),
                                pos.add(1, 0, 1),
                                pos.add(1, 0, 0)
                        };
                for (BlockPos p : doubleZ)
                {
                    if (BlastResistantBlocks.isBlastResistant(p))
                    {
                        resistant++;
                    }
                    else if (BlastResistantBlocks.isUnbreakable(p))
                    {
                        unbreakable++;
                    }
                }
                if (resistant != 6 && unbreakable != 6 && resistant + unbreakable != 6)
                {
                    return null;
                }
                Hole doubleZHole = new Hole(pos, resistant == 6 ? HoleType.OBSIDIAN :
                        unbreakable == 6 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK,
                        pos1, pos2, pos3);
                doubleZHole.addHoleOffsets(doubleZ);
                return doubleZHole;
            }
            // Double X hole, player can stand in the middle of the blocks
            // to prevent placements on these blocks
            else if (air4 && BlockUtil.isBlockAccessible(pos4))
            {
                BlockPos[] doubleX = new BlockPos[]
                        {
                                pos.add(0, 0, 1),
                                pos.add(1, 0, 1),
                                pos.add(2, 0, 0),
                                pos.add(1, 0, -1)
                        };
                for (BlockPos p : doubleX)
                {
                    if (BlastResistantBlocks.isBlastResistant(p))
                    {
                        resistant++;
                    }
                    else if (BlastResistantBlocks.isUnbreakable(p))
                    {
                        unbreakable++;
                    }
                }
                if (resistant != 6 && unbreakable != 6 && resistant + unbreakable != 6)
                {
                    return null;
                }
                Hole doubleXHole = new Hole(pos, resistant == 6 ? HoleType.OBSIDIAN :
                        unbreakable == 6 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK,
                        pos1, pos2, pos4);
                doubleXHole.addHoleOffsets(doubleX);
                return doubleXHole;
            }
            // Standard hole, player can stand in them to prevent
            // large amounts of explosion damage
            else
            {
                if (BlastResistantBlocks.isBlastResistant(pos3))
                {
                    resistant++;
                }
                else if (BlastResistantBlocks.isUnbreakable(pos3))
                {
                    unbreakable++;
                }
                if (BlastResistantBlocks.isBlastResistant(pos4))
                {
                    resistant++;
                }
                else if (BlastResistantBlocks.isUnbreakable(pos4))
                {
                    unbreakable++;
                }
                if (resistant != 4 && unbreakable != 4 && resistant + unbreakable != 4)
                {
                    return null;
                }
                return new Hole(pos, resistant == 4 ? HoleType.OBSIDIAN :
                        unbreakable == 4 ? HoleType.BEDROCK : HoleType.OBSIDIAN_BEDROCK,
                        pos1, pos2, pos3, pos4);
            }
        }
        return null;
    }

    /**
     * @return
     */
    public Set<Hole> getHoles()
    {
        if (result != null)
        {
            try
            {
                holes = result.get();
            }
            catch (ExecutionException | InterruptedException e)
            {

            }
        }
        return holes;
    }

    public class HoleTask implements Callable<Set<Hole>>
    {
        private final List<BlockPos> blocks;

        public HoleTask(List<BlockPos> blocks)
        {
            this.blocks = blocks;
        }

        @Override
        public Set<Hole> call() throws Exception
        {
            Set<Hole> holes1 = new HashSet<>();
            for (BlockPos blockPos : blocks)
            {
                Hole hole = checkHole(blockPos);
                if (hole != null)
                {
                    holes1.add(hole);
                }
            }
            return holes1;
        }
    }
}
