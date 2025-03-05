package net.shoreline.client.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
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
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.BoxRender;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderBlockOutlineEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linus
 * @since 1.0
 */
public class BlockHighlightModule extends ToggleModule
{

    Config<BoxRender> boxModeConfig = register(new EnumConfig<>("BoxMode", "Box rendering mode", BoxRender.OUTLINE, BoxRender.values()));
    Config<Boolean> entitiesConfig = register(new BooleanConfig("Debug-Entities", "Highlights entity bounding boxes for debug purposes", false));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the highlight", 1.0f, 1.0f, 5.0f));
    Config<Boolean> fadeConfig = register(new BooleanConfig("Fade", "Fades the block highlight", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 200, 1000, () -> false));

    private double distance;

    private final Map<Box, Animation> fadeList = new HashMap<>();

    public BlockHighlightModule()
    {
        super("BlockHighlight", "Highlights the block the player is facing", ModuleCategory.RENDER);
    }

    @Override
    public String getModuleData()
    {
        if (mc.world == null)
        {
            return super.getModuleData();
        }
        if (mc.crosshairTarget instanceof BlockHitResult result && mc.world.getBlockState(result.getBlockPos()).isAir())
        {
            return super.getModuleData();
        }
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(distance);
    }

    @Override
    public void onDisable()
    {
        fadeList.clear();
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.world == null)
        {
            return;
        }

        if (!fadeConfig.getValue() && !fadeList.isEmpty())
        {
            fadeList.clear();
        }

        Box render = null;
        final HitResult result = mc.crosshairTarget;
        if (result != null)
        {
            final Vec3d pos = Managers.POSITION.getEyePos();
            if (entitiesConfig.getValue()
                    && result.getType() == HitResult.Type.ENTITY)
            {
                final Entity entity = ((EntityHitResult) result).getEntity();
                render = entity.getBoundingBox();
                distance = pos.distanceTo(entity.getPos());
                if (fadeConfig.getValue())
                {
                    fadeList.put(render, new Animation(true, fadeTimeConfig.getValue()));
                }
            }
            else if (result.getType() == HitResult.Type.BLOCK)
            {
                BlockPos hpos = ((BlockHitResult) result).getBlockPos();
                BlockState state = mc.world.getBlockState(hpos);
                VoxelShape outlineShape = state.getOutlineShape(mc.world, hpos);
                if (outlineShape.isEmpty())
                {
                    return;
                }
                Box render1 = outlineShape.getBoundingBox();
                render = new Box(hpos.getX() + render1.minX, hpos.getY() + render1.minY,
                        hpos.getZ() + render1.minZ, hpos.getX() + render1.maxX,
                        hpos.getY() + render1.maxY, hpos.getZ() + render1.maxZ);
                distance = pos.distanceTo(hpos.toCenterPos());
                if (fadeConfig.getValue())
                {
                    fadeList.put(render, new Animation(true, fadeTimeConfig.getValue()));
                }
            }
        }
        RenderBuffers.preRender();
        if (fadeConfig.getValue())
        {
            for (Map.Entry<Box, Animation> set : fadeList.entrySet())
            {
                Box box = set.getKey();
                set.getValue().setState(false);
                if (set.getValue().getFactor() < 0.01f)
                {
                    continue;
                }
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (145 * set.getValue().getFactor());
                renderBb(event.getMatrices(), box, ColorsModule.getInstance().getRGB(boxAlpha), ColorsModule.getInstance().getRGB(lineAlpha));
            }
        }
        else if (render != null)
        {
            renderBb(event.getMatrices(), render, ColorsModule.getInstance().getRGB(40), ColorsModule.getInstance().getRGB(145));
        }
        RenderBuffers.postRender();
    }

    private void renderBb(MatrixStack matrixStack, Box render, int color, int lineColor)
    {
        switch (boxModeConfig.getValue())
        {
            case FILL ->
            {
                RenderManager.renderBox(matrixStack, render, color);
                RenderManager.renderBoundingBox(matrixStack,
                        render, widthConfig.getValue(), lineColor);
            }
            case OUTLINE -> RenderManager.renderBoundingBox(matrixStack,
                    render, widthConfig.getValue(), lineColor);
        }
    }

    @EventListener
    public void onRenderBlockOutline(RenderBlockOutlineEvent event)
    {
        event.cancel();
    }
}
