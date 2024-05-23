package net.shoreline.client.init;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.Shoreline;
import net.shoreline.client.impl.font.CFontRenderer;
import net.shoreline.client.impl.font.VanillaTextRenderer;
import org.joml.Matrix4f;

import java.io.IOException;

public class Fonts {
    //
    public static final VanillaTextRenderer VANILLA = new VanillaTextRenderer();
    public static TextRenderer CLIENT;
    //
    private static boolean initialized;

    public static void init() {
        try
        {
            if (MinecraftClient.getInstance().getResourceManager() != null)
            {
                CLIENT = CFontRenderer.getTextRender("verdana");
                Shoreline.info("Loaded fonts!");
                initialized = true;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // throw new RuntimeException(e);
        }
    }

    public static void draw(MatrixStack matrices, String text, float x, float y, int color, boolean shadow) {
        draw(text, x, y, color, matrices.peek().getPositionMatrix(), shadow);
    }

    private static void draw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow) {
        if (text == null) {
            return;
        }
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(
                Tessellator.getInstance().getBuffer());
        Fonts.CLIENT.draw(text, x, y, color, shadow, matrix,
                immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        immediate.draw();
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
