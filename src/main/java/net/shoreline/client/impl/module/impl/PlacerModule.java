package net.shoreline.client.impl.module.impl;

import net.minecraft.block.Block;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.interact.Interaction;
import net.shoreline.client.impl.interact.StrictDirection;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlacerModule extends Toggleable
{
    protected final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    private final Map<BlockPos, Animation> fadeOutAnimations = new ConcurrentHashMap<>();

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

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        Managers.RENDER.startRender();
        for (Map.Entry<BlockPos, Animation> animations : fadeOutAnimations.entrySet())
        {
            animations.getValue().setState(false);
            BlockPos blockPos = animations.getKey();
            float boxAlpha = (float) (40 * animations.getValue().getFactor()) / 255.0f;
            int color = ColorUtil.withTransparency(0x5f5fde, boxAlpha);
            Managers.RENDER.renderBox(event.getMatrixStack(), blockPos, -1);
        }

        Managers.RENDER.endRender();
    }
}
