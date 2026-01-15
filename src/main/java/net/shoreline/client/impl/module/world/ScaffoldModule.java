package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.module.impl.PlacerModule;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScaffoldModule extends PlacerModule
{
    Config<Float> placeRange = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("Range to place blocks").build();
    Config<Boolean> keepYConfig = new BooleanConfig.Builder("KeepY")
            .setDescription("Maintains the player's y height")
            .setDefaultValue(false).build();
    Config<Collection<Block>> blockList = new RegistryConfig.Builder<Block>("Blocks")
            .setRegistry(Registries.BLOCK)
            .setValues(Blocks.OBSIDIAN, Blocks.DIRT)
            .setDescription("The blocks to use for scaffolding").build();

    private int groundPosY = Integer.MIN_VALUE;

    private BlockPos lastPlacement;
    private Block currentScaffoldBlock;

    public ScaffoldModule()
    {
        super("Scaffold", new String[] {"BlockFly"}, "Places blocks under the player", GuiCategory.WORLD);
    }

    @Override
    public void onDisable()
    {
        groundPosY = Integer.MIN_VALUE;
        lastPlacement = null;
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        int slot = findValidBlockSlot();
        if (slot == -1)
        {
            return;
        }

        int posY = (int) Math.round(mc.player.getY());
        if (keepYConfig.getValue() && InputUtil.isInputtingMovement())
        {
            if (mc.player.isOnGround() || groundPosY < mc.world.getBottomY())
            {
                groundPosY = posY;
            }

            posY = groundPosY;
        }

        BlockPos pos = new BlockPos(mc.player.getBlockX(), posY, mc.player.getBlockZ());
        createPlacementsFromPositions(currentScaffoldBlock, getScaffoldPlacements(pos.down()), 4.0);
        if (placements.isEmpty() || !Managers.INTERACT.startPlacement(slot))
        {
            return;
        }

        for (BlockPos blockPos : placements)
        {
            placeBlock(blockPos, currentScaffoldBlock, true, false);
            lastPlacement = blockPos;
        }

        Managers.INTERACT.endPlacement();
    }

    private int findValidBlockSlot()
    {
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem && isValidBlock(blockItem.getBlock()))
            {
                currentScaffoldBlock = blockItem.getBlock();
                return i;
            }
        }

        return -1;
    }

    private boolean isValidBlock(Block block)
    {
        return !block.getDefaultState().isReplaceable();
    }

    private List<BlockPos> getScaffoldPlacements(BlockPos playerPos)
    {
        List<BlockPos> placements = new ArrayList<>();
        placements.add(playerPos);

        if (lastPlacement == null || AirPlaceModule.INSTANCE.isEnabled())
        {
            return placements;
        }

        int x0 = lastPlacement.getX();
        int y0 = lastPlacement.getY();
        int z0 = lastPlacement.getZ();
        int x1 = playerPos.getX();
        int y1 = playerPos.getY();
        int z1 = playerPos.getZ();

        int dx = x1 - x0;
        int dy = y1 - y0;
        int dz = z1 - z0;
        int sx = Integer.compare(dx, 0);
        int sy = Integer.compare(dy, 0);
        int sz = Integer.compare(dz, 0);

        dx = Math.abs(dx);
        dy = Math.abs(dy);
        dz = Math.abs(dz);

        int ax = dx << 1;
        int ay = dy << 1;
        int az = dz << 1;

        int steps = 0;
        if (dx >= dy && dx >= dz)
        {
            int yd = ay - dx;
            int zd = az - dx;
            while (true)
            {
                if (!ensurePlaceableWithSupport(new BlockPos(x0, y0, z0), placements))
                {
                    break;
                }

                if (++steps > 8 || (x0 == x1 && y0 == y1 && z0 == z1))
                {
                    break;
                }

                if (yd >= 0)
                {
                    y0 += sy;
                    yd -= ax;
                }

                if (zd >= 0)
                {
                    z0 += sz;
                    zd -= ax;
                }

                x0 += sx;
                yd += ay;
                zd += az;
            }
        } else if (dy >= dx && dy >= dz)
        {
            int xd = ax - dy;
            int zd = az - dy;
            while (true)
            {
                if (!ensurePlaceableWithSupport(new BlockPos(x0, y0, z0), placements))
                {
                    break;
                }

                if (++steps > 8 || (x0 == x1 && y0 == y1 && z0 == z1))
                {
                    break;
                }

                if (xd >= 0)
                {
                    x0 += sx;
                    xd -= ay;
                }

                if (zd >= 0)
                {
                    z0 += sz;
                    zd -= ay;
                }

                y0 += sy;
                xd += ax;
                zd += az;
            }
        } else
        {
            int xd = ax - dz;
            int yd = ay - dz;
            while (true)
            {
                if (!ensurePlaceableWithSupport(new BlockPos(x0, y0, z0), placements))
                {
                    break;
                }

                if (++steps > 8 || (x0 == x1 && y0 == y1 && z0 == z1))
                {
                    break;
                }

                if (xd >= 0)
                {
                    x0 += sx;
                    xd -= az;
                }

                if (yd >= 0)
                {
                    y0 += sy;
                    yd -= az;
                }

                z0 += sz;
                xd += ax;
                yd += ay;
            }
        }

        return placements;
    }

    private boolean ensurePlaceableWithSupport(BlockPos pos, List<BlockPos> out)
    {
        if (!mc.world.getBlockState(pos).isReplaceable())
        {
            return false;
        }

        Direction face = InteractDirection.getInteractDirection(pos);
        if (face != null)
        {
            return false;
        }

        BlockPos support = getSupportingBlock(pos);
        if (support != null)
        {
            double dist = mc.player.squaredDistanceTo(support.toCenterPos());
            if (!out.contains(support) && dist <= placeRange.getValue() * placeRange.getValue())
            {
                out.add(support);
            }
        } else
        {
            BlockPos down = pos.down();
            int depth = 0;
            while (depth++ < 3 && mc.world.getBlockState(down).isReplaceable())
            {
                double dist = mc.player.squaredDistanceTo(down.toCenterPos());
                if (!out.contains(down) && dist <= placeRange.getValue() * placeRange.getValue())
                {
                    out.add(down);
                }

                down = down.down();
            }
        }

        double dist = mc.player.squaredDistanceTo(pos.toCenterPos());
        if (!out.contains(pos) && dist <= placeRange.getValue() * placeRange.getValue())
        {
            out.add(pos);
        }

        return true;
    }

    private BlockPos getSupportingBlock(BlockPos pos)
    {
        for (Direction dir : Direction.Type.HORIZONTAL)
        {
            BlockPos blockPos = pos.offset(dir);
            if (InteractDirection.getInteractDirection(blockPos) != null)
            {
                return blockPos;
            }
        }

        return null;
    }
}
