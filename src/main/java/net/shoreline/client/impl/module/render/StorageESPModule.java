package net.shoreline.client.impl.module.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.*;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class StorageESPModule extends ToggleModule
{
    private static StorageESPModule INSTANCE;

    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The ESP render range", 10.0f, 50.0f, 200.0f));
    Config<Boolean> fillConfig = register(new BooleanConfig("Fill", "Fills in the highlight", false));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the highlight", 1.0f, 1.5f, 5.0f));
    Config<Boolean> chestsConfig = register(new BooleanConfig("Chests", "Render players through walls", true));
    Config<Color> chestsColorConfig = register(new ColorConfig("ChestsColor", "The render color for chests", new Color(200, 200, 101), false, false, () -> chestsConfig.getValue()));
    Config<Boolean> echestsConfig = register(new BooleanConfig("EnderChests", "Render players through walls", true));
    Config<Color> echestsColorConfig = register(new ColorConfig("EnderChestsColor", "The render color for ender chests", new Color(155, 0, 200), false, false, () -> echestsConfig.getValue()));
    Config<Boolean> shulkersConfig = register(new BooleanConfig("Shulkers", "Render players through walls", true));
    Config<Color> shulkersColorConfig = register(new ColorConfig("ShulkersColor", "The render color for shulkers", new Color(200, 0, 106), false, false, () -> shulkersConfig.getValue()));
    Config<Boolean> hoppersConfig = register(new BooleanConfig("Hoppers", "Render players through walls", false));
    Config<Color> hoppersColorConfig = register(new ColorConfig("HoppersColor", "The render color for hoppers", new Color(100, 100, 100), false, false, () -> hoppersConfig.getValue()));
    Config<Boolean> furnacesConfig = register(new BooleanConfig("Furnaces", "Render players through walls", false));
    Config<Color> furnacesColorConfig = register(new ColorConfig("FurnacesColor", "The render color for furnaces", new Color(100, 100, 100), false, false, () -> furnacesConfig.getValue()));

    public StorageESPModule()
    {
        super("StorageESP", "Highlights containers in the world", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static StorageESPModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        RenderBuffers.preRender();
        for (BlockEntity blockEntity : BlockUtil.blockEntities())
        {
            double dist = mc.player.squaredDistanceTo(blockEntity.getPos().toCenterPos());
            if (dist > ((NumberConfig) rangeConfig).getValueSq())
            {
                continue;
            }
            Color color = getStorageESPColor(blockEntity);
            if (color == null)
            {
                continue;
            }

            Vec3d vec3d = blockEntity.getPos().toCenterPos();
            double alpha = (100.0 - mc.player.squaredDistanceTo(vec3d)) / 100.0;
            alpha = 1.0 - MathHelper.clamp(alpha, 0.0, 1.0);
            BlockPos blockPos = blockEntity.getPos();

            if (blockEntity instanceof ChestBlockEntity)
            {
                double x1 = blockPos.getX() + 0.06;
                double y1 = blockPos.getY();
                double z1 = blockPos.getZ() + 0.06;
                double x2 = blockPos.getX() + 0.94;
                double y2 = blockPos.getY() + 0.875;
                double z2 = blockPos.getZ() + 0.94;
                BlockState state = blockEntity.getCachedState();
                if (state.contains(ChestBlock.CHEST_TYPE))
                {
                    Direction direction = state.get(ChestBlock.FACING);
                    ChestType type = state.get(ChestBlock.CHEST_TYPE);
                    if (type == ChestType.RIGHT)
                    {
                        direction = direction.rotateYCounterclockwise();
                        if (direction.getOffsetX() < 0)
                        {
                            x1 += direction.getOffsetX();
                        }
                        else
                        {
                            x2 += direction.getOffsetX();
                        }

                        if (direction.getOffsetZ() < 0)
                        {
                            z1 += direction.getOffsetZ();
                        }
                        else
                        {
                            z2 += direction.getOffsetZ();
                        }
                        int fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (30 * alpha)).getRGB();
                        int outlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (72 * alpha)).getRGB();
                        Box chestBox = new Box(x1, y1, z1, x2, y2, z2);
                        if (fillConfig.getValue())
                        {
                            RenderManager.renderBox(event.getMatrices(), chestBox, fillColor);
                        }
                        RenderManager.renderBoundingBox(event.getMatrices(), chestBox, widthConfig.getValue(), outlineColor);
                    }
                    else if (type == ChestType.LEFT)
                    {
                        direction = direction.rotateYClockwise();
                        if (direction.getOffsetX() < 0)
                        {
                            x1 += direction.getOffsetX();
                        }
                        else
                        {
                            x2 += direction.getOffsetX();
                        }

                        if (direction.getOffsetZ() < 0)
                        {
                            z1 += direction.getOffsetZ();
                        }
                        else
                        {
                            z2 += direction.getOffsetZ();
                        }
                        int fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (30 * alpha)).getRGB();
                        int outlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (72 * alpha)).getRGB();
                        Box chestBox = new Box(x1, y1, z1, x2, y2, z2);
                        if (fillConfig.getValue())
                        {
                            RenderManager.renderBox(event.getMatrices(), chestBox, fillColor);
                        }
                        RenderManager.renderBoundingBox(event.getMatrices(), chestBox, widthConfig.getValue(), outlineColor);
                    }
                    else if (type == ChestType.SINGLE)
                    {
                        int fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (60 * alpha)).getRGB();
                        int outlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (145 * alpha)).getRGB();
                        Box chestBox = new Box(x1, y1, z1, x2, y2, z2);
                        if (fillConfig.getValue())
                        {
                            RenderManager.renderBox(event.getMatrices(), chestBox, fillColor);
                        }
                        RenderManager.renderBoundingBox(event.getMatrices(), chestBox, widthConfig.getValue(), outlineColor);
                    }
                }
            }
            else if (blockEntity instanceof EnderChestBlockEntity)
            {
                double x1 = blockPos.getX() + 0.06;
                double y1 = blockPos.getY();
                double z1 = blockPos.getZ() + 0.06;
                double x2 = blockPos.getX() + 0.94;
                double y2 = blockPos.getY() + 0.875;
                double z2 = blockPos.getZ() + 0.94;
                int fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (60 * alpha)).getRGB();
                int outlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (145 * alpha)).getRGB();
                Box chestBox = new Box(x1, y1, z1, x2, y2, z2);
                if (fillConfig.getValue())
                {
                    RenderManager.renderBox(event.getMatrices(), chestBox, fillColor);
                }
                RenderManager.renderBoundingBox(event.getMatrices(), chestBox, widthConfig.getValue(), outlineColor);
            }
            else
            {
                int fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (60 * alpha)).getRGB();
                int outlineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (145 * alpha)).getRGB();
                if (fillConfig.getValue())
                {
                    RenderManager.renderBox(event.getMatrices(), blockPos, fillColor);
                }
                RenderManager.renderBoundingBox(event.getMatrices(), blockPos, widthConfig.getValue(), outlineColor);
            }
        }
        RenderBuffers.postRender();
    }

    public Color getStorageESPColor(BlockEntity tileEntity)
    {
        if (tileEntity instanceof ChestBlockEntity chestBlockEntity)
        {
            return chestsColorConfig.getValue();
        }
        if (tileEntity instanceof EnderChestBlockEntity)
        {
            return echestsColorConfig.getValue();
        }
        if (tileEntity instanceof ShulkerBoxBlockEntity)
        {
            return shulkersColorConfig.getValue();
        }
        if (tileEntity instanceof HopperBlockEntity)
        {
            return hoppersColorConfig.getValue();
        }
        if (tileEntity instanceof FurnaceBlockEntity)
        {
            return furnacesColorConfig.getValue();
        }
        return null;
    }
}
