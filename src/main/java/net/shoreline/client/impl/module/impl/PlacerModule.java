package net.shoreline.client.impl.module.impl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.interact.PlaceInteraction;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.client.InteractionsModule;
import net.shoreline.client.impl.render.animation.Animation;
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

    public PlacerModule(String name, String description, GuiCategory category)
    {
        this(name, new String[0], description, category);
    }

    public PlacerModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
        addListener(WorldEvent.Disconnect.class, e -> disable());
        addListener(RenderWorldEvent.Post.class, e -> renderBlockPlacements(e.getMatrixStack()));
    }

    protected boolean placeBlock(BlockPos placePos, Block block)
    {
        return placeBlock(placePos, block, !interactConfig.getNoGlitchBlocks().getValue(), interactConfig.getStrictDirection().getValue());
    }

    protected boolean placeBlock(BlockPos placePos, Block block, boolean clientInteract, boolean strictDir)
    {
        final PlaceInteraction placeInteraction = PlaceInteraction.builder()
                .pos(placePos)
                .direction(InteractDirection.getInteractDirection(placePos, strictDir))
                .hand(Hand.MAIN_HAND)
                .block(block)
                .clientInteract(clientInteract)
                .build();

        boolean result = Managers.INTERACT.placeBlock(placeInteraction);
        if (result)
        {
            fadeOutAnimations.put(placePos, new Animation(true, 500));
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

    // Squid retarded headass
    protected void fakePlace(BlockPos blockPos, BlockState blockState)
    {
        MinecraftServer server = mc.getServer();
        if (server == null)
        {
            return;
        }

        Managers.INTERACT.playBlockPlaceSound(blockPos, blockState);
        server.execute(() ->
        {
            ServerWorld world = server.getWorld(mc.world.getRegistryKey());
            if (world == null)
            {
                return;
            }

            world.setBlockState(
                    blockPos,
                    blockState,
                    Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD
            );
        });

        fadeOutAnimations.put(blockPos, new Animation(true, 500));
    }

    protected boolean canPlaceBlock(BlockPos blockPos, Block block)
    {
        return mc.world.getBlockState(blockPos).isReplaceable() && Managers.INTERACT.canPlaceBlock(blockPos, block);
    }
}
