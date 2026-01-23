package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

@Getter
public class InteractionsModule extends Concurrent
{
    public static InteractionsModule INSTANCE;

    Config<Boolean> multiTask = new BooleanConfig.Builder("Multitask")
            .setDescription("Allow using items while interacting")
            .setDefaultValue(true).build();
    Config<Boolean> interactRotate = new BooleanConfig.Builder("Rotate")
            .setDescription("Rotates to face before interacting")
            .setDefaultValue(false).build();
    Config<Boolean> noGlitchBlocks = new BooleanConfig.Builder("NoGlitchBlocks")
            .setDescription("Only spawns blocks when server confirms")
            .setDefaultValue(true).build();
    Config<Boolean> attackCrystals = new BooleanConfig.Builder("AttackCrystal")
            .setDescription("Attacks crystals blocking placements")
            .setDefaultValue(false).build();
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only places on visible faces")
            .setDefaultValue(false).build();
    Config<Boolean> simulation = new BooleanConfig.Builder("Simulate")
            .setDescription("Simulates a block placement to prevent movement flags")
            .setDefaultValue(false).build();


    Config<Integer> bptConfig = new NumberConfig.Builder<Integer>("InteractsPer")
            .setMin(1).setMax(100).setDefaultValue(2)
            .setDescription("The max interactions per interval").build();
    Config<Boolean> intervalMode = new BooleanConfig.Builder("UseThreshold")
            .setDescription("Limits the placements by interval instead of tick")
            .setDefaultValue(false).build();
    Config<Integer> interactInterval = new NumberConfig.Builder<Integer>("Interval")
            .setMin(50).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The interval between interactions")
            .setVisible(() -> intervalMode.getValue()).build();
    Config<Integer> interactDelay = new NumberConfig.Builder<Integer>("Delay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between interactions").build();
    Config<Integer> interactAttempts = new NumberConfig.Builder<Integer>("Limit")
            .setMin(0).setMax(100).setDefaultValue(20)
            .setDescription("Max attempts to interact on blocks").build();

    public InteractionsModule()
    {
        super("Interactions", "Manages world interactions", GuiCategory.CLIENT);
        INSTANCE = this;
    }
}
