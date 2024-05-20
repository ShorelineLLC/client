package net.shoreline.client.init;

import net.shoreline.client.impl.shaders.GradientProgram;
import net.shoreline.client.impl.shaders.RoundedRectangleProgram;

/**
 * @author 06ED
 * @since 1.0
 */
public class Programs {
    public static GradientProgram GRADIENT;
    public static RoundedRectangleProgram ROUNDED_RECTANGLE;

    public static void initPrograms() {
         GRADIENT = new GradientProgram();
         ROUNDED_RECTANGLE = new RoundedRectangleProgram();
    }
}
