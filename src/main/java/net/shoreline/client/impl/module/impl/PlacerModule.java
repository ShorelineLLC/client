package net.shoreline.client.impl.module.impl;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.interact.Interaction;
import net.shoreline.client.impl.interact.StrictDirection;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlacerModule extends Toggleable
{
    protected final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    private final ConcurrentMap<BlockPos, Animation> fadeOutAnimations = new ConcurrentHashMap<>();

    public PlacerModule(String name, String description, GuiCategory category) {
        super(name, description, category);
    }

    public PlacerModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    protected void placeBlock(BlockPos placePos, Block block)
    {
        final Interaction interaction = Interaction.builder()
                .pos(placePos)
                .direction(StrictDirection.getInteractDirection(placePos))
                .hand(Hand.MAIN_HAND)
                .block(block)
                .build();

        Managers.INTERACT.placeBlock(interaction);
        fadeOutAnimations.put(placePos, new Animation(true, 250));
    }

    protected void runSingleBlockPlacement(BlockPos placePos, Block block, int slot)
    {
        if (!Managers.INTERACT.startPlacement(slot))
        {
            return;
        }

        placeBlock(placePos, block);
        Managers.INTERACT.endPlacement();
    }

    protected List<BlockPos> getPlacements(Block block, List<BlockPos> posList, double range)
    {
        final List<BlockPos> placements = new ArrayList<>();
        if (posList.isEmpty())
        {
            return placements;
        }

        for (BlockPos blockPos : posList)
        {
            double dist = mc.player.squaredDistanceTo(blockPos.toCenterPos());
            if (dist > range * range)
            {
                continue;
            }

            if (!mc.world.getBlockState(blockPos).isReplaceable())
            {
                continue;
            }

            if (!Managers.INTERACT.canPlaceBlock(blockPos, block))
            {
                continue;
            }

            placements.add(blockPos);
        }

        return placements;
    }

    public void renderBlockPlacements(MatrixStack matrixStack)
    {
        for (Map.Entry<BlockPos, Animation> animations : fadeOutAnimations.entrySet())
        {
            if (animations.getValue().getFactor() <= 0.01)
            {
                fadeOutAnimations.remove(animations.getKey());
            }

            animations.getValue().setState(false);
            BlockPos blockPos = animations.getKey();
            int color = ThemeModule.INSTANCE.getPrimaryColor().getRGB();
            Managers.RENDER.renderBoundingBox(matrixStack, blockPos, ColorUtil.withTransparency(color, (float) (0.75f * animations.getValue().getFactor())));
            Managers.RENDER.renderBox(matrixStack, blockPos, ColorUtil.withTransparency(color, (float) (0.3f * animations.getValue().getFactor())));
        }
    }
}
