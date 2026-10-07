package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.gui.screen.MouseDraggedEvent;
import net.shoreline.client.impl.event.gui.screen.RenderTooltipEvent;
import net.shoreline.client.impl.inventory.SilentSwapType;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
public class InventoryModule extends Concurrent
{
    public static InventoryModule INSTANCE;

    Config<SilentSwapType> silentSwap = new EnumConfig.Builder<SilentSwapType>("SilentSwap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for silent swapping to items")
            .setDefaultValue(SilentSwapType.HOTBAR).build();
    Config<Boolean> assumeEnchanted = new BooleanConfig.Builder("AssumeBestArmor")
            .setDescription("Assumes that all enemy armor is max enchanted")
            .setDefaultValue(false).build();
    Config<Boolean> dragQuickMove = new BooleanConfig.Builder("DragQuickMove")
            .setDescription("Allows you to drag quick move items in the inventory")
            .setDefaultValue(false).build();

    Config<Boolean> mapTooltips = new BooleanConfig.Builder("Maps")
            .setDescription("Shows contents of maps in the inventory screen")
            .setDefaultValue(false).build();
    Config<Boolean> shulkerTooltips = new BooleanConfig.Builder("Shulkers")
            .setDescription("Shows contents of shulkers in the inventory screen")
            .setDefaultValue(false).build();
    Config<Void> tooltipsConfig = new ConfigGroup.Builder("Tooltips")
            .addAll(mapTooltips, shulkerTooltips)
            .setDescription("Shows extra tooltips in the inventory screen").build();

    public InventoryModule()
    {
        super("Inventory", "Manages inventory interactions", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    @EventListener
    public void onRenderTooltip(RenderTooltipEvent event)
    {
        if (mapTooltips.getValue() && event.getStack().getItem() instanceof FilledMapItem)
        {
//            event.cancel();
//            MatrixStack matrixStack = event.getContext().getMatrices();
//            matrixStack.push();
//            matrixStack.translate(0.0f, 0.0f, 600.0f);
//
//            int x = event.getX();
//            int y = event.getY();
//            int color = ClickGuiModule.INSTANCE.getTheme().getTitleColor();
//            Managers.RENDER.drawRect(matrixStack, x + 8.0f, y - 21.0f, 128.0f, 13.0f, color);
//
//            RenderManager.enableScissor(event.getX() + 8.0,
//                    event.getY() - 21.0, event.getX() + 132.0, event.getY() - 8.0);
//            RenderManager.renderText(event.getContext(), stack.getName().getString(),
//                    event.getX() + 11.0f, event.getY() - 18.0f, -1);
//            RenderManager.disableScissor();
//
//            event.getContext().getMatrices().translate(event.getX() + 8.0f, event.getY() - 8.0f, 0.0f);
//            MapIdComponent mapIdComponent = event.getStack().get(DataComponentTypes.MAP_ID);
//            MapState mapState = FilledMapItem.getMapState(mapIdComponent, mc.world);
//            if (mapState != null)
//            {
//                mc.getMapRenderer().draw(mapState, matrixStack, mapIdComponent, true, 1);
//            }
//
//            matrixStack.pop();
        }
    }

    @EventListener
    public void onMouseDragged(MouseDraggedEvent event)
    {
        if (dragQuickMove.getValue())
        {
            event.cancel();
        }
    }

    public SilentSwapType getSilentSwapType()
    {
        return silentSwap.getValue();
    }
}
