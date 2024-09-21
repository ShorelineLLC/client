package net.shoreline.client.impl.module.render;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.gui.RenderTooltipEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.util.world.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class TooltipsModule extends ToggleModule
{

    // Config<Boolean> enderChestsConfig = register(new BooleanConfig("EnderChests", "Renders all the contents of ender chests in tooltips", false);
    Config<Boolean> shulkersConfig = register(new BooleanConfig("Shulkers", "Renders all the contents of shulkers in tooltips", true));
    Config<Boolean> mapsConfig = register(new BooleanConfig("Maps", "Renders a preview of maps in tooltips", false));

    public TooltipsModule()
    {
        super("Tooltips", "Renders detailed tooltips showing items", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderTooltip(RenderTooltipEvent event)
    {
        final ItemStack stack = event.getStack();
        if (stack.isEmpty())
        {
            return;
        }
        ContainerComponent compoundTag = stack.get(DataComponentTypes.CONTAINER);
        NbtComponent nbtComponent = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (shulkersConfig.getValue() && compoundTag != null && nbtComponent != null && ItemUtil.isShulker(stack.getItem()))
        {
            event.cancel();
            event.context.getMatrices().push();
            event.context.getMatrices().translate(0.0f, 0.0f, 600.0f);
            DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
            NbtCompound nbtCompound = nbtComponent.copyNbt();
            NbtList nbtList = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE);
            List<ItemStack> stacks = compoundTag.stream().toList();
            for (int i = 0; i < nbtList.size(); ++i)
            {
                NbtCompound nbtCompound1 = nbtList.getCompound(i);
                int j = nbtCompound1.getByte("Slot") & 0xFF;
                if (j < 0 || j >= defaultedList.size())
                {
                    continue;
                }
                defaultedList.set(j, stacks.get(i));
            }
            RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                    event.getY() - 21.0, 150.0, 13.0, ColorsModule.getInstance().getRGB(170));

            RenderManager.enableScissor(event.getX() + 8.0,
                    event.getY() - 21.0, event.getX() + 158.0, event.getY() - 8.0);
            RenderManager.renderText(event.getContext(), stack.getName().getString(),
                    event.getX() + 11.0f, event.getY() - 18.0f, -1);
            RenderManager.disableScissor();

            RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                    event.getY() - 8.0, 150.0, 55.0, 0x77000000);
            for (int i = 0; i < defaultedList.size(); i++)
            {
                event.context.drawItem(defaultedList.get(i), event.getX() + (i % 9) * 16 + 9, event.getY() + (i / 9) * 16 - 5);
                event.context.drawItemInSlot(mc.textRenderer, defaultedList.get(i),
                        event.getX() + (i % 9) * 16 + 9, event.getY() + (i / 9) * 16 - 5);
            }
            event.context.getMatrices().pop();
        }
        else if (mapsConfig.getValue() && stack.getItem() instanceof FilledMapItem)
        {
            event.cancel();
            event.context.getMatrices().push();
            event.context.getMatrices().translate(0.0f, 0.0f, 600.0f);
            RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                    event.getY() - 21.0, 128.0, 13.0, ColorsModule.getInstance().getRGB(170));

            RenderManager.enableScissor(event.getX() + 8.0,
                    event.getY() - 21.0, event.getX() + 132.0, event.getY() - 8.0);
            RenderManager.renderText(event.getContext(), stack.getName().getString(),
                    event.getX() + 11.0f, event.getY() - 18.0f, -1);
            RenderManager.disableScissor();

            event.getContext().getMatrices().translate(event.getX() + 8.0f, event.getY() - 8.0f, 0.0f);
            MapIdComponent mapIdComponent = stack.get(DataComponentTypes.MAP_ID);
            MapState mapState = FilledMapItem.getMapState(mapIdComponent, mc.world);
            if (mapState != null)
            {
                mc.gameRenderer.getMapRenderer().draw(event.getContext().getMatrices(), event.getContext().getVertexConsumers(), mapIdComponent, mapState, true, 1);
            }

            event.context.getMatrices().pop();
        }
    }
}
