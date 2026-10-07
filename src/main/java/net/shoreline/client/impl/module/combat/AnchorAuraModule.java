package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.interact.PlaceInteraction;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.combat.anchor.AnchorManager;
import net.shoreline.client.impl.module.combat.anchor.AnchorData;
import net.shoreline.client.impl.module.combat.anchor.AnchorScanner;
import net.shoreline.client.impl.module.impl.PlacerModule;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.*;

@Getter
public class AnchorAuraModule extends PlacerModule
{
    Config<Integer> timeout = new NumberConfig.Builder<Integer>("Timeout")
            .setMin(50).setDefaultValue(250).setMax(2000)
            .setDescription("The time we will use before we consider a anchor unbreakable.").build();
    Config<Float> maxSelfPlace = new NumberConfig.Builder<Float>("MaxSelfPlace")
            .setMin(0.0f).setDefaultValue(8.0f).setMax(36.0f)
            .setDescription("If self damage is over this value a position will not be considered.")
            .build();
    Config<Float> minDamage = new NumberConfig.Builder<Float>("MinDamage")
            .setMin(0.f).setDefaultValue(7.f).setMax(36.f)
            .setDescription("Minimum damage a position needs to deal to a player to be valid").build();
    Config<Void> place = new ConfigGroup.Builder("Place")
            .addAll(maxSelfPlace, minDamage).build();

    Config<Float> maxSelfBreak = new NumberConfig.Builder<Float>("MaxSelfPlace")
            .setMin(0.0f).setDefaultValue(10.0f).setMax(36.0f)
            .setDescription("If self damage is over this value a position will not be considered.")
            .build();
    Config<Float> minBreakDamage = new NumberConfig.Builder<Float>("MinBreakDmg")
            .setMin(0.f).setDefaultValue(2.0f).setMax(36.f)
            .setDescription("Minimum damage a position needs to deal to a player to be valid").build();
    Config<Void> explode = new ConfigGroup.Builder("Break")
            .addAll(maxSelfBreak, minBreakDamage).build();

    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(0.f).setDefaultValue(8.f).setMax(12.f).setFormat("m")
            .setDescription("Range to find targets").build();
    Config<Float> anchorRangeConfig = new NumberConfig.Builder<Float>("AnchorRange")
            .setMin(0.f).setDefaultValue(4.f).setMax(6.f).setFormat("m")
            .setDescription("Range to explode anchors").build();
    Config<Boolean> ignoreTerrain = new BooleanConfig.Builder("IgnoreTerrain")
            .setDefaultValue(true).build();
    Config<Integer> extrapolateConfig = new NumberConfig.Builder<Integer>("Extrapolate")
            .setMin(0).setDefaultValue(0).setMax(20)
            .setDescription("Extrapolation for movement").build();


    /** Manages anchor calculations */
    private final AnchorManager manager;
    /** For mode Fast. A Map of placed anchors that we can explode */
    private final Map<BlockPos, AnchorData> placed = new TreeMap<>();
    /** The latest data. If no valid data was found last tick this will return null */
    private Collection<AnchorData> latestData;

    public AnchorAuraModule()
    {
        super("AnchorAura", "Automatically places and explodes anchors", GuiCategory.COMBAT);
        this.manager = new AnchorManager(this);
    }

    @EventListener
    public void onTick_Pre(TickEvent.Pre event)
    {
        latestData = null;
        if (checkNull())
        {
            return;
        }

        latestData = manager.getResults();
    }

    @EventListener
    public void onUpdate(PlayerUpdateEvent.Pre event)
    {
        Iterator<Map.Entry<BlockPos, AnchorData>> it = placed.entrySet().iterator();
        while (it.hasNext())
        {
            AnchorData data = it.next().getValue();
            if (System.currentTimeMillis() - data.getTime()
                    > timeout.getValue())
            {
                it.remove();
                continue;
            }

            if (Managers.INTERACT.check(data.getPos()))
            {
                continue;
            }

            boolean[] place = place(data);
            if (place[1])
            {
                it.remove();
                return;
            }
        }

        if (latestData == null)
        {
            return;
        }

        for (AnchorData data : latestData)
        {
            BlockPos pos = data.getPos();
            if (data.isAnchor())
            {
                PlayerEntity target = data.getTarget();
                BlockState state = mc.world.getBlockState(pos);
                if (state.getBlock() != Blocks.RESPAWN_ANCHOR)
                {
                    continue;
                }

                AnchorScanner scanner = manager.getScanner();
                float damage     = scanner.getDamage(pos, target);
                float selfDamage = scanner.getDamage(pos, mc.player);

                if (selfDamage > maxSelfBreak.getValue())
                {
                    continue;
                }

                if (damage > minBreakDamage.getValue()
                        || damage > target.getHealth() + target.getAbsorptionAmount())
                {
                    placed.put(pos, data.copy());
                }
            }
            else
            {
                int slot = InventoryUtil.getItemSlot(Items.RESPAWN_ANCHOR);
                if (slot == -1)
                {
                    break;
                }

                if (runSingleBlockPlacement(pos, Blocks.RESPAWN_ANCHOR, slot))
                {
                    placed.put(pos, data.copy());
                }
            }

            break;
        }
    }

    public boolean[] place(AnchorData data)
    {
        boolean[] result = new boolean[]{false, false};
        BlockState state = mc.world.getBlockState(data.getPos());
        if (state.getBlock() != Blocks.RESPAWN_ANCHOR)
        {
            return result;
        }

        int charges = state.get(RespawnAnchorBlock.CHARGES);
        int slot;
        if (charges <= 0)
        {
            slot = InventoryUtil.getHotbarItem(Items.GLOWSTONE).getSlot();
        }
        else
        {
            slot = InventoryUtil.getHotbarItem(stack -> !(stack.getItem() instanceof BlockItem)).getSlot();
            result[1] = true;
        }

        if (!Managers.INTERACT.startPlacement(slot))
        {
            return new boolean[]{false, false};
        }

        PlaceInteraction placeInteraction = new PlaceInteraction.Builder()
                .pos(data.getPos())
                .direction(InteractDirection.getInteractDirection(data.getPos(), isStrictDirection()))
                .hand(Hand.MAIN_HAND)
                .block(Blocks.RESPAWN_ANCHOR)
                .build();

        if (placeInteraction.getDirection() == null)
        {
            Managers.INTERACT.endPlacement();
            return result;
        }

        Vec3d hitVec = placeInteraction.getInteractVec();
        if (interactConfig.getInteractRotate().getValue())
        {
            float[] rots = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitVec);
            Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
        }

        BlockHitResult bhr = new BlockHitResult(placeInteraction.getInteractVec(), placeInteraction.getDirection(), placeInteraction.getPos(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        Managers.INTERACT.endPlacement();
        result[0] = true;
        return result;
    }

    public boolean isStrictDirection()
    {
        return interactConfig.getStrictDirection().getValue();
    }
}
