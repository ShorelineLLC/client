package net.shoreline.client.api.render.layers;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorRenderPhase;
import net.shoreline.client.util.Globals;
import org.lwjgl.opengl.GL11;

import java.util.function.BiFunction;
import java.util.function.Function;

public class RenderLayersClient implements Globals
{
    public static final Identifier SHULKER_BOXES_ATLAS_TEXTURE = Identifier.of("textures/atlas/shulker_boxes.png");
    public static final Identifier CHEST_ATLAS_TEXTURE = Identifier.of("textures/atlas/chest.png");

    public static final RenderLayer GLINT = RenderLayer.of("glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK).cull(RenderPhase.DISABLE_CULLING).depthTest(new DepthTest()).transparency(RenderPhase.GLINT_TRANSPARENCY).texturing(RenderPhase.GLINT_TEXTURING).build(false));
    // Using custom lightmap for 3d rendering
    public static final Function<Identifier, RenderLayer> ENTITY_NO_OUTLINE = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_NO_OUTLINE_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).cull(RenderLayer.DISABLE_CULLING).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).writeMaskState(RenderLayer.COLOR_MASK).build(false);
        return RenderLayer.of("entity_no_outline", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, false, true, multiPhaseParameters);
    });
    public static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_TRANSLUCENT = Util.memoize((texture, affectsOutline) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_TRANSLUCENT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).cull(RenderLayer.DISABLE_CULLING).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(affectsOutline);
        return RenderLayer.of("entity_translucent", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
    });
    public static final Function<Identifier, RenderLayer> ENTITY_SOLID = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_SOLID_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.NO_TRANSPARENCY).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_solid", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, false, multiPhaseParameters);
    });
    public static final Function<Identifier, RenderLayer> ITEM_ENTITY_TRANSLUCENT_CULL_2 = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ITEM_ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).target(RenderLayer.ITEM_ENTITY_TARGET).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).writeMaskState(RenderPhase.ALL_MASK).build(true);
        return RenderLayer.of("item_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
    });
    public static final Function<Identifier, RenderLayer> ENTITY_TRANSLUCENT_CULL = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
    });
    public static final Function<Identifier, RenderLayer> ENTITY_CUTOUT = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_CUTOUT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.NO_TRANSPARENCY).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_cutout", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, false, multiPhaseParameters);
    });
    public static final Function<Identifier, RenderLayer> ENTITY_CUTOUT_NO_CULL = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_CUTOUT_NONULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.NO_TRANSPARENCY).cull(RenderLayer.DISABLE_CULLING).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_cutout_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, false, multiPhaseParameters);
    });
    public static final BiFunction<Identifier, Boolean, RenderLayer> ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize((texture, affectsOutline) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_CUTOUT_NONULL_OFFSET_Z_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.NO_TRANSPARENCY).cull(RenderLayer.DISABLE_CULLING).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).layering(RenderLayer.VIEW_OFFSET_Z_LAYERING).build(affectsOutline);
        return RenderLayer.of("entity_cutout_no_cull_z_offset", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, false, multiPhaseParameters);
    });
    public static final Function<Identifier, RenderLayer> TEXT = Util.memoize((texture) -> {
        return RenderLayer.of("text", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 786432, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TEXT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).build(false));
    });
    public static final RenderLayer TEXT_BACKGROUND = RenderLayer.of("text_background", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TEXT_BACKGROUND_PROGRAM).texture(RenderLayer.NO_TEXTURE).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).build(false));;
    public static final Function<Identifier, RenderLayer> TEXT_INTENSITY = Util.memoize((texture) -> {
        return RenderLayer.of("text_intensity", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 786432, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TEXT_INTENSITY_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).build(false));
    });
    public static final Function<Identifier, RenderLayer> TEXT_POLYGON_OFFSET = Util.memoize((texture) -> {
        return RenderLayer.of("text_polygon_offset", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TEXT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).layering(RenderLayer.POLYGON_OFFSET_LAYERING).build(false));
    });
    public static final Function<Identifier, RenderLayer> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize((texture) -> {
        return RenderLayer.of("text_intensity_polygon_offset", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TEXT_INTENSITY_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).layering(RenderLayer.POLYGON_OFFSET_LAYERING).build(false));
    });
    public static final Function<Identifier, RenderLayer> TEXT_SEE_THROUGH = Util.memoize((texture) -> {
        return RenderLayer.of("text_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TRANSPARENT_TEXT_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).depthTest(RenderLayer.ALWAYS_DEPTH_TEST).writeMaskState(RenderLayer.COLOR_MASK).build(false));
    });
    public static final RenderLayer TEXT_BACKGROUND_SEE_THROUGH = RenderLayer.of("text_background_see_through", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TRANSPARENT_TEXT_BACKGROUND_PROGRAM).texture(RenderLayer.NO_TEXTURE).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).depthTest(RenderLayer.ALWAYS_DEPTH_TEST).writeMaskState(RenderLayer.COLOR_MASK).build(false));
    public static final Function<Identifier, RenderLayer> TEXT_INTENSITY_SEE_THROUGH = Util.memoize((texture) -> {
        return RenderLayer.of("text_intensity_see_through", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.TRANSPARENT_TEXT_INTENSITY_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).depthTest(RenderLayer.ALWAYS_DEPTH_TEST).writeMaskState(RenderLayer.COLOR_MASK).build(false));
    });

    protected static class DepthTest extends RenderPhase.DepthTest
    {
        public DepthTest()
        {
            super("depth_test", GL11.GL_ALWAYS);
        }

        @Override
        public void startDrawing()
        {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glDepthFunc(GL11.GL_EQUAL);
        }

        @Override
        public void endDrawing()
        {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            // GL11.glClearDepth(1.0);
        }
    }

    protected static class Lightmap extends RenderPhase.Lightmap
    {
        public Lightmap()
        {
            super(false);
            ((AccessorRenderPhase) this).hookSetBeginAction(() ->
            {
                Managers.LIGHT_MAP.enable();
            });
            ((AccessorRenderPhase) this).hookSetEndAction(() ->
            {
                Managers.LIGHT_MAP.disable();
            });
        }
    }
}