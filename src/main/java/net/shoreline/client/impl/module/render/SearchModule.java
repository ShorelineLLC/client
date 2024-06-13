package net.shoreline.client.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BlockListConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linus
 * @since 1.0
 */
public class SearchModule extends ToggleModule
{
    Config<List<Item>> whitelistConfig = register(new BlockListConfig<>("Whitelist", "Valid search blocks", Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.ENDER_CHEST, Blocks.CHEST,
            Blocks.TRAPPED_CHEST, Blocks.DISPENSER, Blocks.DROPPER, Blocks.SHULKER_BOX, Blocks.SPAWNER, Blocks.END_PORTAL_FRAME));

    private final Map<BlockPos, BlockState> blocks = new ConcurrentHashMap<>();

    public SearchModule()
    {
        super("Search", "Highlights specified blocks in the world", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        if (mc.world == null)
        {
            return;
        }
        for (int x = 125; x > -125; --x)
        {
            for (int y = 125; y > -125; --y)
            {
                for (int z = 125; z > -125; --z)
                {
                    BlockPos blockPos = BlockPos.ofFloored(mc.player.getX() + x,
                            mc.player.getY() + y, mc.player.getZ() + z);
                    BlockState state = mc.world.getBlockState(blockPos);
                    if (isSearchBlock(state))
                    {
                        blocks.remove(blockPos);
                        blocks.put(blockPos, state);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable()
    {
        blocks.clear();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof ChunkDataS2CPacket packet)
        {
            if (mc.world == null)
            {
                return;
            }
            int chunkX = (packet.getChunkX() * 16) - 16;
            int chunkZ = (packet.getChunkZ() * 16) - 16;
            for (int x = chunkX; x < chunkX + 16; x++)
            {
                for (int z = chunkZ; z < chunkZ + 16; z++)
                {
                    for (int y = mc.world.getBottomY(); y < mc.world.getHeight(); y++)
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = mc.world.getBlockState(pos);
                        if (isSearchBlock(state))
                        {
                            blocks.remove(pos);
                            blocks.put(pos, state);
                        }
                    }
                }
            }
        }
        else if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
        {
            if (isSearchBlock(packet.getState()))
            {
                blocks.remove(packet.getPos());
                blocks.put(packet.getPos(), packet.getState());
            }
            else
            {
                blocks.remove(packet.getPos());
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        RenderBuffers.preRender();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet())
        {
            BlockPos pos = entry.getKey();
            VoxelShape outlineShape = entry.getValue().getOutlineShape(mc.world, pos);
            if (outlineShape.isEmpty())
            {
                return;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(pos.getX() + render1.minX, pos.getY() + render1.minY,
                    pos.getZ() + render1.minZ, pos.getX() + render1.maxX,
                    pos.getY() + render1.maxY, pos.getZ() + render1.maxZ);
            RenderManager.renderBox(event.getMatrices(), render, ColorsModule.getInstance().getRGB(40));
            RenderManager.renderBoundingBox(event.getMatrices(),
                    render, 2.5f, ColorsModule.getInstance().getRGB(145));
        }
        RenderBuffers.postRender();
    }

    private boolean isSearchBlock(BlockState state)
    {
        return ((BlockListConfig) whitelistConfig).contains(state.getBlock());
    }
}
