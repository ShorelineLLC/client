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

import java.util.function.Function;

public class RenderLayersClient implements Globals
{
    public static final RenderLayer GLINT = RenderLayer.of("glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK).cull(RenderPhase.DISABLE_CULLING).depthTest(new DepthTest()).transparency(RenderPhase.GLINT_TRANSPARENCY).texturing(RenderPhase.GLINT_TEXTURING).build(false));

    public static final Function<Identifier, RenderLayer> ENTITY_TRANSLUCENT_CULL = Util.memoize((texture) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(new RenderPhase.Texture(texture, false, false)).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).lightmap(new Lightmap()).overlay(RenderLayer.ENABLE_OVERLAY_COLOR).build(true);
        return RenderLayer.of("entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, true, true, multiPhaseParameters);
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
            ((AccessorRenderPhase) this).hookSetBeginAction(() -> Managers.LIGHT_MAP.enable());
            ((AccessorRenderPhase) this).hookSetEndAction(() -> Managers.LIGHT_MAP.disable());
        }
    }
}