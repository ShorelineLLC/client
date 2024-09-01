package net.shoreline.client.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class PhaseESPModule extends ToggleModule
{

    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the render", 1.0f, 1.0f, 5.0f));
    Config<Boolean> diagonalConfig = register(new BooleanConfig("Diagonal", "Renders safe diagonal phase blocks", true));
    Config<Boolean> safeConfig = register(new BooleanConfig("Safe", "Highlights safe phase blocks", false));
    Config<Color> unsafeConfig = register(new ColorConfig("UnsafeColor", "The color for rendering unsafe phase blocks", new Color(255, 0, 0), false, false));
    Config<Color> obsidianConfig = register(new ColorConfig("ObsidianColor", "The color for rendering obsidian phase blocks", new Color(255, 255, 0), false, false, () -> safeConfig.getValue()));
    Config<Color> bedrockConfig = register(new ColorConfig("BedrockColor", "The color for rendering bedrock phase blocks", new Color(0, 255, 0), false, false, () -> safeConfig.getValue()));

    public PhaseESPModule()
    {
        super("PhaseESP", "Displays safe phase blocks", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.player == null || mc.world == null || !mc.player.isOnGround())
        {
            return;
        }
        RenderBuffers.preRender();
        BlockPos playerPos = mc.player.getBlockPos();
        Vec3d pos = mc.player.getPos();
        double dx = pos.getX() - playerPos.getX();
        double dz = pos.getZ() - playerPos.getZ();
        for (Direction direction : Direction.values())
        {
            if (!direction.getAxis().isHorizontal())
            {
                continue;
            }
            BlockPos blockPos = playerPos.offset(direction);
            if (mc.world.getBlockState(blockPos).isReplaceable())
            {
                continue;
            }

            Color color = getPhaseColor(blockPos);
            if (color == null)
            {
                continue;
            }
            double x = blockPos.getX();
            double y = blockPos.getY();
            double z = blockPos.getZ();
            if (direction == Direction.EAST && dx >= 0.65)
            {
                RenderManager.renderLine(event.getMatrices(), x, y, z, x, y, z + 1.0, widthConfig.getValue(), color.getRGB());
            }
            else if (direction == Direction.WEST && dx <= 0.35)
            {
                RenderManager.renderLine(event.getMatrices(), x + 1.0, y, z, x + 1.0, y, z + 1.0, widthConfig.getValue(), color.getRGB());
            }
            else if (direction == Direction.SOUTH && dz >= 0.65)
            {
                RenderManager.renderLine(event.getMatrices(), x, y, z, x + 1.0, y, z, widthConfig.getValue(), color.getRGB());
            }
            else if (direction == Direction.NORTH && dz <= 0.35)
            {
                RenderManager.renderLine(event.getMatrices(), x, y, z + 1.0, x + 1.0, y, z + 1.0, widthConfig.getValue(), color.getRGB());
            }
        }

        if (diagonalConfig.getValue())
        {
            double x = playerPos.getX();
            double y = playerPos.getY();
            double z = playerPos.getZ();
            BlockPos currentPos = playerPos.offset(Direction.WEST).offset(Direction.NORTH);
            Color color1 = getPhaseColor(currentPos);
            if (color1 != null && dx <= 0.35 && dz <= 0.35)
            {
                RenderManager.renderLine(event.getMatrices(), x, y, z, x, y + 1.0, z, widthConfig.getValue(), color1.getRGB());
            }
            currentPos = playerPos.offset(Direction.WEST).offset(Direction.SOUTH);
            Color color2 = getPhaseColor(currentPos);
            if (color2 != null && dx <= 0.35 && dz >= 0.65)
            {
                RenderManager.renderLine(event.getMatrices(), x, y, z + 1.0, x, y + 1.0, z + 1.0, widthConfig.getValue(), color2.getRGB());
            }
            currentPos = playerPos.offset(Direction.EAST).offset(Direction.NORTH);
            Color color3 = getPhaseColor(currentPos);
            if (color3 != null && dx >= 0.65 && dz <= 0.35)
            {
                RenderManager.renderLine(event.getMatrices(), x + 1.0, y, z, x + 1.0, y + 1.0, z, widthConfig.getValue(), color3.getRGB());
            }
            currentPos = playerPos.offset(Direction.EAST).offset(Direction.SOUTH);
            Color color4 = getPhaseColor(currentPos);
            if (color4 != null && dx >= 0.65 && dz >= 0.65)
            {
                RenderManager.renderLine(event.getMatrices(), x + 1.0, y, z + 1.0, x + 1.0, y + 1.0, z + 1.0, widthConfig.getValue(), color4.getRGB());
            }
        }

        RenderBuffers.postRender();
    }

    private Color getPhaseColor(BlockPos blockPos)
    {
        BlockState state1 = mc.world.getBlockState(blockPos);
        if (state1.isAir())
        {
            return null;
        }
        BlockState state = mc.world.getBlockState(blockPos.down());
        Color color = null;
        if (state.isReplaceable())
        {
            color = unsafeConfig.getValue();
        }
        else if (safeConfig.getValue())
        {
            if (BlastResistantBlocks.isUnbreakable(state.getBlock()))
            {
                color = bedrockConfig.getValue();
            }
            else
            {
                color = obsidianConfig.getValue();
            }
        }
        return color;
    }
}