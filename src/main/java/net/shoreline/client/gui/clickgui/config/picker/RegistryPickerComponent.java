package net.shoreline.client.gui.clickgui.config.picker;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.components.SearchComponent;
import net.shoreline.client.gui.clickgui.components.ToggleComponent;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegistryPickerComponent<T> extends ExpandableComponent<Collection<T>>
{
    private final SearchComponent searchBar;
    private final ToggleComponent[] cells = new ToggleComponent[12];
    private final Object[] cellEntries = new Object[12];
    private float pickerHeight;

    public RegistryPickerComponent(Config<Collection<T>> config,
                                   ModuleComponent moduleComponent,
                                   Frame frame,
                                   float x,
                                   float y,
                                   float frameWidth,
                                   float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.searchBar = new SearchComponent(frame, x, y, frameWidth, frameHeight);
        for (int i = 0; i < cells.length; i++)
        {
            RegistryConfig<T> reg = (RegistryConfig<T>) config;
            Object entry = cellEntries[i];

            final int idx = i;
            cells[i] = new ToggleComponent(frame, 0, 0, 0, 0, reg.contains((T) entry), () ->
            {
                Object e = cellEntries[idx];
                if (e == null)
                {
                    return cells[idx].getState();
                }

                boolean in = reg.contains((T) e);
                if (in)
                {
                    reg.remove((T) e);
                    return false;
                } else
                {
                    reg.add((T) e);
                    return true;
                }
            });
        }
    }

    @Override
    public void drawComponent(DrawContext context, float mouseX, float mouseY, float delta)
    {
        boolean hovering = Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height);
        setHoverState(hovering);
        if (hovering)
        {
            ClickGuiScreen.INSTANCE.setDescriptionText(getConfig().getDescription());
        }

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();
        float scale = ClickGuiModule.INSTANCE.getScale();

        int base = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, base);

        drawText(context, getConfig().getName(), getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());

        String summary = getConfig().getValue().size() + " selected";
        drawText(context, Formatting.GRAY + summary,
                getTx() + width - 3.0f - getTextWidth(summary),
                getTy() + 4.0f, theme.getTextColor());

        float t = (float) collapseAnim.getFactor();
        if (t <= 0.001f)
        {
            return;
        }

        final float pad = 6.0f * scale;
        final float searchH = 14.0f * scale;
        final float cell = 22.0f * scale;
        final float gap = 4.0f * scale;

        List<T> results = findResults(searchBar.getQuery());
        int slots = Math.min(results.size(), 12);
        int rows = (int) Math.ceil(slots / 4.0f);
        rows = Math.max(rows, 1);

        float gridH = rows * cell + (rows - 1) * gap;
        float targetH = pad + searchH + pad + gridH + pad;

        float lerp = Math.min(1.0f, (delta <= 0 ? 0.16f : delta * 10.0f));
        pickerHeight += (targetH - pickerHeight) * lerp;

        float panelX = getTx();
        float panelY = getTy() + height;
        float panelW = width;
        float panelH = pickerHeight * t;

        int panelBg = ColorUtil.brighten(0x00101010, 80, 1f);
        drawRect(context, panelX, panelY, panelW, panelH, panelBg);

        float searchX = panelX + pad;
        float searchY = panelY + pad;

        searchBar.setYOffset(0.0f);
        searchBar.setX(searchX);
        searchBar.setY(searchY);
        searchBar.setWidth(panelW - pad * 2.0f);
        searchBar.setHeight(searchH);
        searchBar.drawComponent(context, mouseX, mouseY, delta);

        float gridWCells = 4.0f * cell + 3.0f * gap;
        float gridX = panelX + (panelW - gridWCells) / 2.0f;
        float gridY = searchY + searchH + pad;

        float availableH = Math.max(0.0f, panelH - (gridY - panelY) - pad);
        enableScissor(context, gridX, gridY, gridX + gridWCells, gridY + availableH);

        for (int i = 0; i < cells.length; i++)
        {
            cellEntries[i] = null;
        }

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < 4; c++)
            {
                int idx = r * 4 + c;
                float cellX = gridX + c * (cell + gap);
                float cellY = gridY + r * (cell + gap);

                ToggleComponent tg = cells[idx];
                tg.setX(cellX);
                tg.setY(cellY);
                tg.setWidth(cell);
                tg.setHeight(cell);

                if (idx < results.size())
                {
                    T entry = results.get(idx);
                    updateDescription(entry, tg);
                    cellEntries[idx] = entry;
                    tg.drawComponent(context, mouseX, mouseY, delta);
                    int ix = (int) (cellX + (cell - 16.0f) / 2.0f);
                    int iy = (int) (cellY + (cell - 16.0f) / 2.0f);
                    renderEntry(context, entry, ix, iy);

                    RegistryConfig<T> reg = (RegistryConfig<T>) getConfig();
                    tg.setState(reg.contains(entry));
                } else
                {
                    tg.drawComponent(context, mouseX, mouseY, delta);
                }
            }
        }

        disableScissor(context);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.pickerOpen = !pickerOpen;
            collapseAnim.setState(pickerOpen);
            collapseAnim.setEasing(pickerOpen ? Easing.CUBIC_OUT : Easing.CUBIC_IN);
            return;
        }

        if (!pickerOpen)
        {
            return;
        }

        float scale = ClickGuiModule.INSTANCE.getScale();
        final int cols = 4;
        final int maxRows = 3;
        final float pad = 4.0f * scale;
        final float searchH = 14.0f * scale;
        final float cell = 22.0f * scale;
        final float gap = 4.0f * scale;

        float panelX = getTx();
        float panelY = getTy() + height + 2.0f;
        float panelW = width;

        float searchX = panelX + pad;
        float searchY = panelY + pad;
        float searchW = panelW - pad * 2.0f;

        if (Mouse.isHovering(mouseX, mouseY, searchX, searchY, searchW, searchH))
        {
            searchBar.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            List<T> results = findResults(searchBar.getQuery());
            int slots = Math.min(results.size(), cols * maxRows);
            int rows = Math.max(1, (int) Math.ceil(slots / (float) cols));

            float gridWCells = cols * cell + (cols - 1) * gap;
            float gridX = panelX + (panelW - gridWCells) / 2.0f;
            float gridY = searchY + searchH + pad;
            float gridH = rows * cell + (rows - 1) * gap;

            if (Mouse.isHovering(mouseX, mouseY, gridX, gridY, gridWCells, gridH))
            {
                for (int i = 0; i < rows * cols; i++)
                {
                    cells[i].mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (pickerOpen)
        {
            searchBar.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char chr, int modifiers)
    {
        if (pickerOpen)
        {
            searchBar.charTyped(chr, modifiers);
        }
    }

    private void updateDescription(T entry, ToggleComponent component)
    {
        if (component.getHoverAnim().getState())
        {
            if (entry instanceof Block block)
            {
                ClickGuiScreen.INSTANCE.setDescriptionText(I18n.translate(block.getTranslationKey()));
            }
            else
            {
                ItemStack stack = toStack(entry);
                ClickGuiScreen.INSTANCE.setDescriptionText(stack.getItem().getName().toString());
            }
        }
    }

    private List<T> findResults(String query)
    {
        RegistryConfig<T> regConfig = (RegistryConfig<T>) getConfig();
        List<T> out = new ArrayList<>(12);
        if (query == null)
        {
            return out;
        }

        String q = query.toLowerCase();
        for (T entry : regConfig.getRegistry())
        {
            if (entry == Blocks.AIR || entry == Items.AIR)
            {
                continue;
            }

            Identifier id = regConfig.getRegistry().getId(entry);
            if (id == null)
            {
                continue;
            }

            String name = id.toString().toLowerCase();
            if (q.isEmpty() || name.contains(q))
            {
                out.add(entry);
                if (out.size() >= 12)
                {
                    break;
                }
            }
        }
        return out;
    }

    private void renderEntry(DrawContext context, T entry, int x, int y)
    {
        ItemStack stack = toStack(entry);
        if (!stack.isEmpty())
        {
            context.drawItem(stack, x, y);
            return;
        }

        if (entry instanceof Block b)
        {
            Sprite blockSprite = getBlockSprite(b);
            if (blockSprite != null)
            {
                context.drawSpriteStretched(RenderLayer::getGuiTextured, blockSprite, x, y, 16, 16);
                return;
            }
        }

        Sprite missing = mc.getBakedModelManager().getMissingBlockModel().getParticleSprite();
        context.drawSpriteStretched(RenderLayer::getGuiTextured, missing, x, y, 16, 16);
    }

    private ItemStack toStack(T entry)
    {
        if (entry instanceof Item it)
        {
            return new ItemStack(it);
        } else if (entry instanceof Block b)
        {
            return new ItemStack(b.asItem());
        }
        return ItemStack.EMPTY;
    }

    private Sprite getBlockSprite(Block block)
    {
        if (block instanceof FluidBlock)
        {
            FluidState fluidState = block.getDefaultState().getFluidState();
            FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getFluid());
            if (handler != null)
            {
                Sprite[] sprites = handler.getFluidSprites(null, null, fluidState);
                if (sprites != null && sprites.length > 0 && sprites[0] != null)
                {
                    return sprites[0];
                }
            }
        }

        BlockState state = block.getDefaultState();
        BakedModel model = mc.getBlockRenderManager().getModel(state);
        if (model != null)
        {
            return model.getParticleSprite();
        }

        return null;
    }

    @Override
    public float getComponentHeight()
    {
        return pickerHeight * (float) collapseAnim.getFactor();
    }
}
