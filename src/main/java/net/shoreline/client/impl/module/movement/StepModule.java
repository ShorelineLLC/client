package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class StepModule extends Toggleable
{
    Config<Boolean> strictConfig = new BooleanConfig.Builder("Strict")
            .setDescription("Works on updated NCP servers")
            .setDefaultValue(false).build();

    public StepModule()
    {
        super("Step", "Step up blocks", GuiCategory.MOVEMENT);
    }

    // Credit: doogie
    private double[] getStepOffsets(double stepHeight)
    {
        double[] offsets = new double[0];
        if (strictConfig.getValue())
        {
            if (stepHeight > 1.1661)
            {
                offsets = new double[] {0.42, 0.7532, 1.001, 1.1661, stepHeight};
            } else if (stepHeight > 1.015)
            {
                offsets = new double[] {0.42, 0.7532, 1.001, stepHeight};
            } else if (stepHeight > 0.6)
            {
                offsets = new double[] {0.42 * stepHeight, 0.7532 * stepHeight, stepHeight};
            }

            return offsets;
        }

        if (stepHeight > 2.019)
        {
            offsets = new double[] {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919};
        } else if (stepHeight > 1.5)
        {
            offsets = new double[] {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        } else if (stepHeight > 1.015)
        {
            offsets = new double[] {0.42, 0.7532, 1.01, 1.093, 1.015};
        } else if (stepHeight > 0.6)
        {
            offsets = new double[] {0.42 * stepHeight, 0.7532 * stepHeight};
        }

        return offsets;
    }
}
