package net.shoreline.client.impl.module.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.mixin.accessor.AccessorWorldRenderer;
import net.shoreline.client.util.world.BlastResistantBlocks;

import java.awt.*;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linus
 * @since 1.0
 */
public class BreakHighlightModule extends ToggleModule {

    Config<HighlightMode> modeConfig = new EnumConfig<>("Mode", "The mode for highlighting blocks", HighlightMode.PACKET, HighlightMode.values());
    Config<Float> rangeConfig = new NumberConfig<>("Range", "The range to render breaking blocks", 5.0f, 10.0f, 50.0f);
    Config<Color> colorConfig = new ColorConfig("Color", "The break highlight color", new Color(255, 0, 0), false, true);
    //
    private final Map<BlockBreakingProgressS2CPacket, Long> breakingProgress = new ConcurrentHashMap<>();

    public BreakHighlightModule() {
        super("BreakHighlight", "Highlights blocks that are being broken", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet && !contains(packet.getPos())
                && !BlastResistantBlocks.isUnbreakable(packet.getPos())) {
            breakingProgress.put(packet, System.currentTimeMillis());
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (modeConfig.getValue() == HighlightMode.NORMAL) {
            Int2ObjectMap<BlockBreakingInfo> blockBreakProgressions =
                    ((AccessorWorldRenderer) mc.worldRenderer).getBlockBreakingProgressions();
            for (Int2ObjectMap.Entry<BlockBreakingInfo> info :
                    Int2ObjectMaps.fastIterable(blockBreakProgressions)) {
                BlockPos pos = info.getValue().getPos();
                double dist = mc.player.squaredDistanceTo(pos.toCenterPos());
                if (dist > ((NumberConfig) rangeConfig).getValueSq()) {
                    continue;
                }
                int damage = info.getValue().getStage();
                BlockState state = mc.world.getBlockState(pos);
                VoxelShape outlineShape = state.getOutlineShape(mc.world, pos);
                if (outlineShape.isEmpty()) {
                    continue;
                }
                Box bb = outlineShape.getBoundingBox();
                bb = new Box(pos.getX() + bb.minX, pos.getY() + bb.minY,
                        pos.getZ() + bb.minZ, pos.getX() + bb.maxX, pos.getY() + bb.maxY, pos.getZ() + bb.maxZ);
                double x = bb.minX + (bb.maxX - bb.minX) / 2.0;
                double y = bb.minY + (bb.maxY - bb.minY) / 2.0;
                double z = bb.minZ + (bb.maxZ - bb.minZ) / 2.0;
                double sizeX = damage * ((bb.maxX - x) / 9.0);
                double sizeY = damage * ((bb.maxY - y) / 9.0);
                double sizeZ = damage * ((bb.maxZ - z) / 9.0);
                RenderManager.renderBox(event.getMatrices(), new Box(x - sizeX,
                        y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ), ColorsModule.INSTANCE.getRGB(60));
                RenderManager.renderBoundingBox(event.getMatrices(), new Box(x - sizeX,
                        y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ), 1.5f, ColorsModule.INSTANCE.getRGB(125));
            }
        } else {
            for (Map.Entry<BlockBreakingProgressS2CPacket, Long> mine : breakingProgress.entrySet()) {
                BlockPos mining = mine.getKey().getPos();
                long elapsedTime = System.currentTimeMillis() - mine.getValue();
                long count = breakingProgress.keySet().stream().filter(p -> p.getEntityId() == mine.getKey().getEntityId()).count();
                while (count > 2) {
                    breakingProgress.entrySet().stream().filter(p -> p.getKey().getEntityId() == mine.getKey().getEntityId())
                            .min(Comparator.comparingLong(Map.Entry::getValue)).ifPresent(min -> breakingProgress.remove(min.getKey(), min.getValue()));
                    count--;
                }
                if (mc.world.isAir(mining) || elapsedTime > 2500) {
                    breakingProgress.remove(mine.getKey(), mine.getValue());
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(mining.toCenterPos());
                if (dist > ((NumberConfig) rangeConfig).getValueSq()) {
                    continue;
                }
                VoxelShape outlineShape = mc.world.getBlockState(mining).getOutlineShape(mc.world, mining);
                outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
                Box render1 = outlineShape.getBoundingBox();
                Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                        mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                        mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
                Vec3d center = render.getCenter();
                float scale = MathHelper.clamp(elapsedTime / 2500.0f, 0.0f, 1.0f);
                double dx = (render1.maxX - render1.minX) / 2.0;
                double dy = (render1.maxY - render1.minY) / 2.0;
                double dz = (render1.maxZ - render1.minZ) / 2.0;
                final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
                RenderManager.renderBox(event.getMatrices(), scaled, ((ColorConfig) colorConfig).getValue(60).getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), scaled, 1.5f, ((ColorConfig) colorConfig).getValue(125).getRGB());
            }
        }
    }

    private boolean contains(BlockPos pos) {
        return breakingProgress.keySet().stream().anyMatch(p -> p.getPos().equals(pos));
    }

    private enum HighlightMode {
        NORMAL,
        PACKET
    }
}
