package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

@Getter
public class AnchorAuraModule extends PlacerModule
{
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
    Config<Boolean> sequential = new BooleanConfig.Builder("Sequential")
            .setDefaultValue(true).build();
    Config<SilentSwapType> silentType = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The silent swap type")
            .setDefaultValue(SilentSwapType.HOTBAR).build();

    /** Manages anchor calculations */
    private final AnchorManager manager;
    /** The latest data. If no valid data was found last tick this will return null */
    private List<AnchorData> latestData;

    public AnchorAuraModule()
    {
        super("AutoAnchor", "Automatically places and explodes anchors", GuiCategory.COMBAT);
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

        AnchorData firstAnchor = null;
        AnchorData firstAir = null;

        for (AnchorData data : latestData)
        {
            if (firstAnchor == null && data.isAnchor())
            {
                firstAnchor = data;
            }
            else if (firstAir == null && !data.isAnchor())
            {
                firstAir = data;
            }

            if (firstAnchor != null && firstAir != null)
            {
                break;
            }
        }

        if (firstAnchor != null)
        {
            BlockState state = mc.world.getBlockState(firstAnchor.getPos());
            if (state.getBlock() != Blocks.RESPAWN_ANCHOR)
            {
                return;
            }

            int charges = state.get(RespawnAnchorBlock.CHARGES);
            int slot = -1;
            if (charges <= 0)
            {
                slot = InventoryUtil.getItemSlot(Items.GLOWSTONE, silentType.getValue());
            }
            else
            {
                slot = InventoryUtil.getHotbarSlot(stack -> !(stack.getItem() instanceof BlockItem));
            }

            if (!Managers.INVENTORY.startSwap(slot))
            {
                return;
            }

            Interaction interaction = Interaction.builder()
                    .pos(firstAnchor.getPos())
                    .direction(InteractDirection.getInteractDirection(firstAnchor.getPos(), interactConfig.getStrictDirection().getValue()))
                    .hand(Hand.MAIN_HAND)
                    .block(Blocks.RESPAWN_ANCHOR)
                    .packetPlace(interactConfig.getNoGlitchBlocks().getValue())
                    .build();

            BlockHitResult result = new BlockHitResult(interaction.getPos().toCenterPos().add(interaction.getHitVec()), interaction.getDirection(), interaction.getPos(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
            Managers.INVENTORY.endSwap();
        }
        else if (firstAir != null)
        {
            int slot = InventoryUtil.getItemSlot(Items.RESPAWN_ANCHOR);
            if (slot == -1)
            {
                return;
            }

            runSingleBlockPlacement(firstAir.getPos(), Blocks.RESPAWN_ANCHOR, slot);
        }
    }
}
