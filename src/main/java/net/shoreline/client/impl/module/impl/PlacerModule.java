package net.shoreline.client.impl.module.impl;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.interact.Interaction;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.client.InteractionsModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.BoxRender;
import net.shoreline.client.impl.render.Easing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlacerModule extends CombatModule
{
    protected final InteractionsModule interactConfig = InteractionsModule.INSTANCE;

    protected final List<BlockPos> placements = new ArrayList<>();
    protected final ConcurrentMap<BlockPos, Animation> fadeOutAnimations = new ConcurrentHashMap<>();

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

    protected boolean placeBlock(BlockPos placePos, Block block)
    {
        return placeBlock(placePos, block, interactConfig.getNoGlitchBlocks().getValue(), interactConfig.getStrictDirection().getValue());
    }

    protected boolean placeBlock(BlockPos placePos, Block block, boolean packetPlace, boolean strictDir)
    {
        final Interaction interaction = Interaction.builder()
                .pos(placePos)
                .direction(InteractDirection.getInteractDirection(placePos, strictDir))
                .hand(Hand.MAIN_HAND)
                .block(block)
                .packetPlace(packetPlace)
                .build();

        boolean result = Managers.INTERACT.placeBlock(interaction);
        if (result)
        {
            addPlaceAnim(placePos);
        }

        return result;
    }

    protected boolean runSingleBlockPlacement(BlockPos placePos, Block block, int slot)
    {
        if (!canPlaceBlock(placePos, block) || !Managers.INTERACT.startPlacement(slot))
        {
            return false;
        }

        boolean result = placeBlock(placePos, block);
        Managers.INTERACT.endPlacement();
        return result;
    }

    protected void createPlacementsFromPositions(Block block, Collection<BlockPos> posList, double range)
    {
        placements.clear();

        if (posList.isEmpty())
        {
            return;
        }

        for (BlockPos blockPos : posList)
        {
            double dist = mc.player.squaredDistanceTo(blockPos.toCenterPos());
            if (dist > range * range)
            {
                continue;
            }

            if (!canPlaceBlock(blockPos, block))
            {
                continue;
            }

            placements.add(blockPos);
        }
    }

    public void renderBlockPlacements(MatrixStack matrixStack)
    {
        for (Map.Entry<BlockPos, Animation> animations : fadeOutAnimations.entrySet())
        {
            if (animations.getValue().getFactor() <= 0.01)
            {
                fadeOutAnimations.remove(animations.getKey());
                continue;
            }

            animations.getValue().setState(false);
            BlockPos blockPos = animations.getKey();
            int color = ThemeModule.INSTANCE.getPrimaryColor().getRGB();

            BoxRender.FILL.render(matrixStack, blockPos, color, (float) Easing.SMOOTH_STEP.ease(animations.getValue().getFactor()));
        }
    }

    protected void addPlaceAnim(BlockPos blockPos)
    {
        fadeOutAnimations.put(blockPos, new Animation(true, 500));
    }

    protected boolean canPlaceBlock(BlockPos blockPos, Block block)
    {
        return mc.world.getBlockState(blockPos).isReplaceable() && Managers.INTERACT.canPlaceBlock(blockPos, block);
    }
}
