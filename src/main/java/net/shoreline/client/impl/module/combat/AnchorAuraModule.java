package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.interact.InteractDirection;
import net.shoreline.client.impl.interact.Interaction;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.module.combat.anchor.AnchorManager;
import net.shoreline.client.impl.module.combat.anchor.AnchorData;
import net.shoreline.client.impl.module.impl.PlacerModule;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.impl.rotation.Rotation;
import net.shoreline.client.impl.rotation.RotationUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Collection;
import java.util.List;

@Getter
public class AnchorAuraModule extends PlacerModule
{
    Config<Float> maxSelfDamageConfig = new NumberConfig.Builder<Float>("MaxSelfDamage")
            .setMin(0.0f).setDefaultValue(8.0f).setMax(36.0f)
            .setDescription("If self damage is over this value a position will not be considered.")
            .build();
    Config<Float> minDamageConfig = new NumberConfig.Builder<Float>("MinDamage")
            .setMin(0.f).setDefaultValue(7.f).setMax(36.f)
            .setDescription("Minimum damage a position needs to deal to a player to be valid").build();
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
    public void onClientRotation(ClientRotationEvent event)
    {
        if (latestData == null || latestData.isEmpty())
        {
            return;
        }

        for (AnchorData data : latestData)
        {
            if (data.isAnchor())
            {
                BlockState state = mc.world.getBlockState(data.getPos());
                if (state.getBlock() != Blocks.RESPAWN_ANCHOR)
                {
                    break;
                }

                int charges = state.get(RespawnAnchorBlock.CHARGES);
                int slot;
                if (charges <= 0)
                {
                    slot = InventoryUtil.getHotbarSlot(Items.GLOWSTONE);
                }
                else
                {
                    slot = InventoryUtil.getHotbarSlot(stack -> !(stack.getItem() instanceof BlockItem));
                }

                if (!Managers.INTERACT.startPlacement(slot))
                {
                    break;
                }

                Interaction interaction = Interaction.builder()
                        .pos(data.getPos())
                        .direction(InteractDirection.getInteractDirection(data.getPos(), isStrictDirection()))
                        .hand(Hand.MAIN_HAND)
                        .block(Blocks.RESPAWN_ANCHOR)
                        .packetPlace(false)
                        .build();

                if (interaction.getDirection() == null)
                {
                    Managers.INTERACT.endPlacement();
                    break;
                }

                Vec3d hitVec = interaction.getPos().toCenterPos().add(interaction.getHitVec());
                if (interactConfig.getInteractRotate().getValue())
                {
                    float[] rots = RotationUtil.getRotationsTo(mc.player.getEyePos(), hitVec);
                    Managers.ROTATION.setSilentRotation(new Rotation(rots[0], rots[1]));
                }

                BlockHitResult result = new BlockHitResult(interaction.getPos().toCenterPos().add(interaction.getHitVec()), interaction.getDirection(), interaction.getPos(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
                Managers.INTERACT.endPlacement();
            }
            else
            {
                int slot = InventoryUtil.getItemSlot(Items.RESPAWN_ANCHOR);
                if (slot == -1)
                {
                    break;
                }

                runSingleBlockPlacement(data.getPos(), Blocks.RESPAWN_ANCHOR, slot);
            }

            break;
        }
    }

    public boolean isStrictDirection()
    {
        return interactConfig.getStrictDirection().getValue();
    }
}
