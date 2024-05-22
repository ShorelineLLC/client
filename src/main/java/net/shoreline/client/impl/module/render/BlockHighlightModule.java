package net.shoreline.client.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.BoxRender;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderBlockOutlineEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;

import java.text.DecimalFormat;

/**
 * @author linus
 * @since 1.0
 */
public class BlockHighlightModule extends ToggleModule {

    Config<BoxRender> boxModeConfig = register(new EnumConfig<>("BoxMode", "Box rendering mode", BoxRender.OUTLINE, BoxRender.values()));
    Config<Boolean> entitiesConfig = register(new BooleanConfig("Debug-Entities", "Highlights entity bounding boxes for debug purposes", false));
    private double distance;

    public BlockHighlightModule() {
        super("BlockHighlight", "Highlights the block the player is facing", ModuleCategory.RENDER);
    }

    @Override
    public String getModuleData() {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(distance);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.world == null) {
            return;
        }
        Box render = null;
        final HitResult result = mc.crosshairTarget;
        if (result != null) {
            final Vec3d pos = Managers.POSITION.getEyePos();
            if (entitiesConfig.getValue()
                    && result.getType() == HitResult.Type.ENTITY) {
                final Entity entity = ((EntityHitResult) result).getEntity();
                render = entity.getBoundingBox();
                distance = pos.distanceTo(entity.getPos());
            } else if (result.getType() == HitResult.Type.BLOCK) {
                BlockPos hpos = ((BlockHitResult) result).getBlockPos();
                BlockState state = mc.world.getBlockState(hpos);
                VoxelShape outlineShape = state.getOutlineShape(mc.world, hpos);
                if (outlineShape.isEmpty()) {
                    return;
                }
                Box render1 = outlineShape.getBoundingBox();
                render = new Box(hpos.getX() + render1.minX, hpos.getY() + render1.minY,
                        hpos.getZ() + render1.minZ, hpos.getX() + render1.maxX,
                        hpos.getY() + render1.maxY, hpos.getZ() + render1.maxZ);
                distance = pos.distanceTo(hpos.toCenterPos());
            }
        }
        RenderBuffers.preRender();
        if (render != null) {
            switch (boxModeConfig.getValue()) {
                case FILL -> {
                    RenderManager.renderBox(event.getMatrices(), render,
                            ColorsModule.getInstance().getRGB(60));
                    RenderManager.renderBoundingBox(event.getMatrices(),
                            render, 2.5f, ColorsModule.getInstance().getRGB(145));
                }
                case OUTLINE -> RenderManager.renderBoundingBox(event.getMatrices(),
                        render, 2.5f, ColorsModule.getInstance().getRGB(145));
            }
        }
        RenderBuffers.postRender();
    }

    @EventListener
    public void onRenderBlockOutline(RenderBlockOutlineEvent event) {
        event.cancel();
    }
}
