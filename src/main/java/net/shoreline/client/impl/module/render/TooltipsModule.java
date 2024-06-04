package net.shoreline.client.impl.module.render;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.collection.DefaultedList;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.gui.RenderTooltipEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.eventbus.annotation.EventListener;

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
        super("Tooltips", "Renders detailed tooltips showing items",
                ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderTooltip(RenderTooltipEvent event)
    {
        final ItemStack stack = event.getStack();
        if (stack.isEmpty())
        {
            return;
        }
        NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(stack);
        if (shulkersConfig.getValue() && nbtCompound != null
                && nbtCompound.contains("Items", NbtElement.LIST_TYPE))
        {
            event.cancel();
            event.context.getMatrices().push();
            event.context.getMatrices().translate(0.0f, 0.0f, 600.0f);
            DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(nbtCompound, defaultedList);
            RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                    event.getY() - 21.0, 150.0, 13.0, ColorsModule.getInstance().getRGB(170));
            RenderManager.renderText(event.getContext(), stack.getName().getString(),
                    event.getX() + 11.0f, event.getY() - 18.0f, -1);
            RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                    event.getY() - 7.0, 150.0, 55.0, 0x77000000);
            for (int i = 0; i < defaultedList.size(); i++)
            {
                event.context.drawItem(defaultedList.get(i), event.getX() + (i % 9) * 16 + 9, event.getY() + (i / 9) * 16 - 5);
                event.context.drawItemInSlot(mc.textRenderer, defaultedList.get(i),
                        event.getX() + (i % 9) * 16 + 9, event.getY() + (i / 9) * 16 - 5);
            }
            event.context.getMatrices().pop();
        }
    }
}
