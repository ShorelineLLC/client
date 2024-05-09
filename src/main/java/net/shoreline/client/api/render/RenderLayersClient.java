package net.shoreline.client.api.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.shoreline.client.mixin.accessor.AccessorRenderPhase;
import net.shoreline.client.util.Globals;
import org.lwjgl.opengl.GL11;

public class RenderLayersClient implements Globals {

    public static final VertexFormat POSITION_COLOR_TEXTURE_OVERLAY = new VertexFormat((ImmutableMap) ImmutableMap.builder().put("Position", VertexFormats.POSITION_ELEMENT).put("Color", VertexFormats.COLOR_ELEMENT).put("UV0", VertexFormats.TEXTURE_ELEMENT).put("UV1", VertexFormats.OVERLAY_ELEMENT).put("Padding", VertexFormats.PADDING_ELEMENT).put("UV2", VertexFormats.LIGHT_ELEMENT).put("Normal", VertexFormats.NORMAL_ELEMENT).build());
    //
    public static final RenderLayer GLINT = RenderLayer.of("glint", VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS, 256, RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.GLINT_PROGRAM).texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK).cull(RenderPhase.DISABLE_CULLING).depthTest(new DepthTest()).transparency(RenderPhase.GLINT_TRANSPARENCY).texturing(RenderPhase.GLINT_TEXTURING).build(false));
    // Using custom lightmap for 3d rendering
    public static final RenderLayer ITEM_ENTITY_TRANSLUCENT_CULL = RenderLayer.of("item_entity_translucent_cull", POSITION_COLOR_TEXTURE_OVERLAY, VertexFormat.DrawMode.QUADS, 1536,
            RenderLayer.MultiPhaseParameters.builder().program(RenderPhase.ITEM_ENTITY_TRANSLUCENT_CULL_PROGRAM).texture(RenderPhase.BLOCK_ATLAS_TEXTURE).lightmap(new Lightmap())
                    .target(RenderPhase.ITEM_ENTITY_TARGET).writeMaskState(RenderPhase.ALL_MASK).build(true));


    protected static class DepthTest extends RenderPhase.DepthTest {
        public DepthTest() {
            super("depth_test", GL11.GL_ALWAYS);
        }

        @Override
        public void startDrawing() {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_EQUAL);
        }

        @Override
        public void endDrawing() {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            // GL11.glClearDepth(1.0);
        }
    }

    protected static class Lightmap extends RenderPhase.Lightmap {

        public Lightmap() {
            super(false);
            ((AccessorRenderPhase) this).hookSetBeginAction(() -> {
                mc.gameRenderer.getLightmapTextureManager().enable();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            });
            ((AccessorRenderPhase) this).hookSetEndAction(() -> {
                mc.gameRenderer.getLightmapTextureManager().disable();
            });
        }
    }
}
