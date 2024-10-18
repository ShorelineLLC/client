package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.WorldChunk;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BlockListConfig;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.LoadChunkBlockEvent;
import net.shoreline.client.impl.event.world.LoadWorldEvent;
import net.shoreline.client.impl.event.world.SetBlockStateEvent;
import net.shoreline.client.impl.event.world.UnloadChunkBlocksEvent;
import net.shoreline.client.mixin.accessor.AccessorCamera;
import net.shoreline.client.util.render.RenderUtil;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linus
 * @since 1.0
 */
public class SearchModule extends ToggleModule
{
    Config<List<Block>> blocksConfig = register(new BlockListConfig<>("Blocks", "Valid search blocks",
            Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.DISPENSER,
            Blocks.DROPPER, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.SPAWNER, Blocks.END_PORTAL_FRAME));
    Config<Boolean> tracersConfig = register(new BooleanConfig("Tracers", "Draws tracers to highlighted blocks", false));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the tracer", 1.0f, 1.0f, 5.0f, () -> tracersConfig.getValue()));
    Config<Boolean> fillConfig = register(new BooleanConfig("Fill", "Fills the render", true));
    Config<Boolean> softReloadConfig = register(new BooleanConfig("SoftReload", "Reloads without clearing the renders", false));

    private final Map<BlockPos, BlockState> blocks = new ConcurrentHashMap<>();
    private boolean warnedLag;

    public SearchModule()
    {
        super("Search", "Highlights specified blocks in the world", ModuleCategory.RENDER);
    }

    @Override
    public void onEnable()
    {
        if (mc.world == null || mc.getNetworkHandler() == null)
        {
            return;
        }
        // Big hack
        for (WorldChunk chunk : BlockUtil.loadedChunks())
        {
            int startX = chunk.getPos().getStartX();
            int startZ = chunk.getPos().getStartZ();

            for (int y = chunk.getBottomY(); y < chunk.getHeight(); y++)
            {
                for (int x1 = startX; x1 < startX + 16; x1++)
                {
                    for (int z1 = startZ; z1 < startZ + 16; z1++)
                    {
                        BlockPos pos = new BlockPos(x1, y, z1);
                        BlockState blockState = chunk.getBlockState(pos);
                        if (!blockState.isAir() && isSearchBlock(blockState) && !blocks.containsKey(pos))
                        {
                            blocks.put(pos, blockState);
                        }
                    }
                }
            }
        }
        RenderUtil.reloadRenders(softReloadConfig.getValue());
        if (!warnedLag)
        {
            sendModuleMessage(Formatting.RED + "This module may cause lag when loading new chunks");
            warnedLag = true;
        }
    }

    @Override
    public void onDisable()
    {
        blocks.clear();
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        blocks.clear();
    }

    @EventListener
    public void onChangeDimension(LoadWorldEvent event)
    {
        blocks.clear();
    }

    @EventListener
    public void onLoadChunk(LoadChunkBlockEvent event)
    {
        BlockState blockState = event.getState();
        if (!blockState.isAir() && isSearchBlock(blockState) && !blocks.containsKey(event.getPos()))
        {
            blocks.put(event.getPos(), blockState);
        }
    }

    @EventListener
    public void onUnloadChunkBlocks(UnloadChunkBlocksEvent event)
    {
        blocks.entrySet().removeIf(e -> event.contains(e.getKey()));
    }

    @EventListener
    public void onSetBlockState(SetBlockStateEvent event)
    {
        if (isSearchBlock(event.getState()))
        {
            if (!blocks.containsKey(event.getPos()))
            {
                blocks.put(event.getPos(), event.getState());
            }
        }
        else
        {
            if (blocks.containsKey(event.getPos()))
            {
                blocks.remove(event.getPos());
            }
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getConfig() == blocksConfig && event.getStage() == StageEvent.EventStage.POST)
        {
            disable();
            enable();
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (!(mc.getCameraEntity() instanceof PlayerEntity playerEntity))
        {
            return;
        }
        if (tracersConfig.getValue())
        {
            MatrixStack matrixStack = new MatrixStack();
            double d = mc.options.getFov().getValue();
            matrixStack.multiplyPositionMatrix(mc.gameRenderer.getBasicProjectionMatrix(d));
            Matrix4f prevProjectionMatrix = RenderSystem.getProjectionMatrix();
            RenderSystem.setProjectionMatrix(matrixStack.peek().getPositionMatrix(), VertexSorter.BY_DISTANCE);
            RenderBuffers.preRender();
            Vec3d playerPos = Interpolation.getRenderPosition(playerEntity, event.getTickDelta());
            // interp on camera y pos
            Camera camera = mc.gameRenderer.getCamera();
            double eyeHeight = MathHelper.lerp(event.getTickDelta(), ((AccessorCamera) camera).getLastCameraY(), ((AccessorCamera) camera).getCameraY());
            double x1 = playerEntity.getX() - playerPos.getX();
            double y1 = playerEntity.getY() - playerPos.getY() + eyeHeight;
            double z1 = playerEntity.getZ() - playerPos.getZ();
            float pitch = playerEntity.getPitch();
            float yaw = playerEntity.getYaw();
            if (FreecamModule.getInstance().isEnabled())
            {
                Vec3d pos1 = FreecamModule.getInstance().getCameraPosition();
                Vec3d pos2 = Interpolation.getRenderPosition(pos1, FreecamModule.getInstance().getLastCameraPosition(), event.getTickDelta());
                float rotations[] = FreecamModule.getInstance().getCameraRotations();
                x1 = pos1.x - pos2.x;
                y1 = pos1.y - pos2.y;
                z1 = pos1.z - pos2.z;
                yaw = rotations[0];
                pitch = rotations[1];
            }
            Vec3d pos = new Vec3d(0.0, 0.0, 1.0)
                    .rotateX(-(float) Math.toRadians(pitch))
                    .rotateY(-(float) Math.toRadians(yaw))
                    .add(new Vec3d(x1, y1, z1));
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet())
            {
                BlockPos pos1 = entry.getKey();
                RenderManager.renderLine(event.getMatrices(), pos, pos1.toCenterPos(), widthConfig.getValue(), getColor(pos1, entry.getValue(), 255));
            }
            RenderBuffers.postRender();
            RenderSystem.setProjectionMatrix(prevProjectionMatrix, VertexSorter.BY_DISTANCE);
        }
        RenderBuffers.preRender();
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet())
        {
            BlockPos pos1 = entry.getKey();
            VoxelShape outlineShape = entry.getValue().getOutlineShape(mc.world, pos1);
            if (outlineShape.isEmpty())
            {
                continue;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(pos1.getX() + render1.minX, pos1.getY() + render1.minY,
                    pos1.getZ() + render1.minZ, pos1.getX() + render1.maxX,
                    pos1.getY() + render1.maxY, pos1.getZ() + render1.maxZ);
            if (fillConfig.getValue())
            {
                RenderManager.renderBox(event.getMatrices(), render, getColor(pos1, entry.getValue(), 40));
            }
            RenderManager.renderBoundingBox(event.getMatrices(),
                    render, 1.5f, getColor(pos1, entry.getValue(), 145));
        }
        RenderBuffers.postRender();
    }

    private int getColor(BlockPos pos, BlockState state, int alpha)
    {
        int color = state.getMapColor(mc.world, pos).color;
        Block block = state.getBlock();
        if (block == Blocks.NETHER_PORTAL)
        {
            color = 0xff9307ff;
        }
        int r = ColorHelper.Argb.getRed(color);
        int g = ColorHelper.Argb.getGreen(color);
        int b = ColorHelper.Argb.getBlue(color);
        return new Color(r, g, b, alpha).getRGB();
    }

    private boolean isSearchBlock(BlockState state)
    {
        return ((BlockListConfig) blocksConfig).contains(state.getBlock());
    }
}
