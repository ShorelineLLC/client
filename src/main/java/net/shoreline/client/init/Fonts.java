package net.shoreline.client.init;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.Shoreline;
import net.shoreline.client.impl.font.AWTFontRenderer;
import net.shoreline.client.impl.font.CFontRenderer;
import net.shoreline.client.impl.font.VanillaTextRenderer;
import org.joml.Matrix4f;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Fonts {
    //
    public static final VanillaTextRenderer VANILLA = new VanillaTextRenderer();
    public static AWTFontRenderer CLIENT;
    //
    private static boolean initialized;

    public static void init() {
        CLIENT = new AWTFontRenderer(Shoreline.class.getClassLoader().getResourceAsStream("assets/shoreline/font/verdana.ttf"), 9.0f);
        Shoreline.info("Loaded fonts!");
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
