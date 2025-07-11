package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.inventory.SilentSwapType;

import java.awt.*;

public class SpeedMineModule extends Toggleable
{
    Config<MiningMode> modeConfig = new EnumConfig.Builder<MiningMode>("Mode")
            .setValues(MiningMode.values()).setDefaultValue(MiningMode.NORMAL)
            .setDescription("The mode for block click packets").build();
    Config<Boolean> doubleMine = new BooleanConfig.Builder("DoubleMine")
            .setDescription("Rotates before mining block")
            .setVisible(() -> modeConfig.getValue() == MiningMode.GRIM)
            .setDefaultValue(false).build();
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The max range to mine").build();
    Config<Float> speedConfig = new NumberConfig.Builder<Float>("Progress")
            .setMin(0.1f).setMax(1.0f).setDefaultValue(1.0f)
            .setDescription("The mining progress before breaking").build();
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while mining")
            .setDefaultValue(true).build();
    Config<Boolean> rotateConfig = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates before mining block")
            .setDefaultValue(false).build();
    Config<SilentSwapType> swapConfig = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values())
            .setDescription("The mode for swapping to pearls")
            .setDefaultValue(SilentSwapType.HOTBAR).build();

    Config<Color> miningColor = new ColorConfig.Builder("Mining")
            .setDescription("The color when mining a block")
            .setDefaultValue(Color.RED).build();
    Config<Color> breakingColor = new ColorConfig.Builder("Breaking")
            .setDescription("The color when breaking a block")
            .setDefaultValue(Color.GREEN).build();
    Config<Void> renderConfig = new ConfigGroup.Builder("Render")
            .addAll(miningColor, breakingColor).build();

    public SpeedMineModule()
    {
        super("SpeedMine", new String[] {"SpeedyGonzales"}, "Mine faster", GuiCategory.WORLD);
    }

    private enum MiningMode
    {
        NORMAL,
        GRIM
    }
}
