package net.shoreline.client.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.render.RenderBlockOutlineEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.eventbus.annotation.EventListener;

public class BlockHighlightModule extends RenderModule
{
    Config<BoxRender> modeConfig = new EnumConfig.Builder<BoxRender>("Mode")
            .setValues(BoxRender.values())
            .setDescription("Box rendering mode")
            .setDefaultValue(BoxRender.FILL).build();

    Config<Boolean> debugEntitiesConfig = new BooleanConfig.Builder("Entities")
            .setDescription("Render entity hitboxes for debugging")
            .setDefaultValue(false).build();

    public BlockHighlightModule()
    {
        super("BlockHighlight", "Highlights the block the player is looking at", GuiCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (mc.crosshairTarget instanceof BlockHitResult result)
        {
            BlockPos pos = result.getBlockPos();
            BlockState state = mc.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(mc.world, pos);
            if (!shape.isEmpty())
            {
                for (Box box : shape.getBoundingBoxes())
                {
                    double minX = pos.getX() + box.minX;
                    double minY = pos.getY() + box.minY;
                    double minZ = pos.getZ() + box.minZ;
                    double maxX = pos.getX() + box.maxX;
                    double maxY = pos.getY() + box.maxY;
                    double maxZ = pos.getZ() + box.maxZ;
                    Box bb = new Box(minX, minY, minZ, maxX, maxY, maxZ);
                    modeConfig.getValue().render(event.getMatrixStack(), bb, ThemeModule.INSTANCE.getPrimaryColor().getRGB());
                }
            }
        }
        else if (mc.crosshairTarget instanceof EntityHitResult result && debugEntitiesConfig.getValue())
        {
            Entity entity = result.getEntity();
            if (entity != null)
            {
                modeConfig.getValue().render(event.getMatrixStack(), entity.getBoundingBox(), ThemeModule.INSTANCE.getPrimaryColor().getRGB());
            }
        }
    }

    @EventListener
    public void onRenderBlockOutline(RenderBlockOutlineEvent event)
    {
        event.cancel();
    }
}
