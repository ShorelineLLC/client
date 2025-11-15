package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.interact.Interaction;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.client.impl.module.combat.anchor.AnchorManager;
import net.shoreline.client.impl.module.combat.anchor.AnchorPositionData;
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
    Config<SilentSwapType> silentType = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The silent swap type")
            .setDefaultValue(SilentSwapType.HOTBAR).build();

    /** Manages anchor calculations */
    private final AnchorManager manager;
    /** The latest data. If no valid data was found last tick this will return null */
    private List<AnchorPositionData> latestData;

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

        List<AnchorPositionData> list = manager.getResults();
        if (list.isEmpty())
        {
            return;
        }

        latestData = list;
    }

    @EventListener
    public void onClientRotation(ClientRotationEvent event)
    {
        if (checkNull() || latestData == null || latestData.isEmpty())
        {
            return;
        }

        AnchorPositionData firstAnchor = null;
        AnchorPositionData firstAir = null;

        for (AnchorPositionData data : latestData)
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

            if (slot == -1)
            {
                return;
            }

            if (!Managers.INTERACT.startPlacement(slot))
            {
                return;
            }

            Interaction interaction = Interaction.builder()
                    .pos(firstAnchor.getPos())
                    .direction(Direction.UP)
                    .block(Blocks.RESPAWN_ANCHOR)
                    .build();

            Managers.INTERACT.placeBlock(interaction);
            Managers.INTERACT.endPlacement();
        }
    }
}
