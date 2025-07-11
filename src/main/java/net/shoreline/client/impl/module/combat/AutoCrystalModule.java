package net.shoreline.client.impl.module.combat;

import lombok.Getter;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.module.combat.crystal.CrystalBaseScanner;
import net.shoreline.client.impl.inventory.SilentSwapType;

@Getter
public class AutoCrystalModule extends Toggleable
{
    public static AutoCrystalModule INSTANCE;

    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows using items while interacting")
            .setDefaultValue(true).build();

    Config<Float> targetRange = new NumberConfig.Builder<Float>("TargetRange")
            .setMin(1.0f).setMax(15.0f).setDefaultValue(10.0f).setFormat("m")
            .setDescription("The range to target entities").build();
    Config<Boolean> targetPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Targets players").setDefaultValue(true).build();
    Config<Boolean> targetHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Targets hostiles").setDefaultValue(false).build();
    Config<Boolean> targetPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Targets passives").setDefaultValue(false).build();
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetRange, targetPlayers, targetHostiles, targetPassives).build();

    Config<Float> placeRange = new NumberConfig.Builder<Float>("PlaceRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to place crystals").build();
    Config<Integer> placeDelay = new NumberConfig.Builder<Integer>("PlaceDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between placing crystals").build();
    Config<Boolean> strictDirection = new BooleanConfig.Builder("StrictDirection")
            .setDescription("Only places crystals on visible faces")
            .setDefaultValue(false).build();
    Config<Void> placeConfig = new ConfigGroup.Builder("Place")
            .addAll(placeRange, placeDelay, strictDirection).build();

    Config<Float> breakRange = new NumberConfig.Builder<Float>("BreakRange")
            .setMin(1.0f).setMax(6.0f).setDefaultValue(4.0f).setFormat("m")
            .setDescription("The range to break crystals").build();
    Config<Integer> breakDelay = new NumberConfig.Builder<Integer>("BreakDelay")
            .setMin(0).setMax(1000).setDefaultValue(100).setFormat("ms")
            .setDescription("The delay between breaking crystals").build();
    Config<Void> breakConfig = new ConfigGroup.Builder("Break")
            .addAll(breakRange, breakDelay).build();

    Config<Boolean> targetItems = new BooleanConfig.Builder("TargetItems")
            .setDescription("Targets dropped items blocking placements")
            .setDefaultValue(false).build();
    Config<Void> antiSurroundConfig = new ConfigGroup.Builder("AntiSurround")
            .addAll(targetItems).build();

    Config<Float> minDamage = new NumberConfig.Builder<Float>("MinDamage")
            .setMin(1.0f).setMax(10.0f).setDefaultValue(4.0f)
            .setDescription("The minimum damage to consider crystals").build();
    Config<Float> maxSelfDamage = new NumberConfig.Builder<Float>("MaxSelfDamage")
            .setMin(1.0f).setMax(20.0f).setDefaultValue(12.0f)
            .setDescription("The maximum damage a crystal can do to the player").build();
    Config<Boolean> overrideConfig = new BooleanConfig.Builder("Override")
            .setDescription("Allows overriding minimum damage (e.g. allows crystal spam)")
            .setDefaultValue(true).build();
    Config<Integer> minArmorDamage = new NumberConfig.Builder<Integer>("MinArmorDamage")
            .setMin(0).setMax(100).setDefaultValue(5).setFormat("%")
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("The minimum armor damage to consider spamming crystals").build();
    Config<Float> damageMultiplier = new NumberConfig.Builder<Float>("DamageMultiplier")
            .setMin(1.0f).setMax(5.0f).setDefaultValue(1.0f)
            .setVisible(() -> overrideConfig.getValue())
            .setDescription("Place if we can kill target in this many crystals").build();
    Config<Void> damageConfig = new ConfigGroup.Builder("Damage")
            .addAll(minDamage, maxSelfDamage, overrideConfig, minArmorDamage, damageMultiplier).build();

    Config<Boolean> rotatePacket = new BooleanConfig.Builder("SilentRotate")
            .setDescription("Rotates before placing crystals")
            .setDefaultValue(false).build();
    Config<Float> yawLimit = new NumberConfig.Builder<Float>("FovLimit")
            .setMin(1.0f).setMax(180.0f).setDefaultValue(180.0f).setFormat("deg")
            .setVisible(() -> rotatePacket.getValue())
            .setDescription("The range to target entities").build();
    Config<Void> rotateConfig = new ConfigGroup.Builder("Rotate")
            .addAll(rotatePacket, yawLimit).build();

    Config<Boolean> autoSwap = new BooleanConfig.Builder("AutoSwap")
            .setDescription("Automatically swaps to crystals before placing")
            .setDefaultValue(false).build();
    Config<Boolean> antiWeakness = new BooleanConfig.Builder("AntiWeakness")
            .setDescription("Swaps to sword before attacking crystals")
            .setDefaultValue(false).build();
    Config<SilentSwapType> silentSwap = new EnumConfig.Builder<SilentSwapType>("Swap")
            .setValues(SilentSwapType.values()).setDefaultValue(SilentSwapType.HOTBAR)
            .setDescription("The silent swap mode for placing crystals")
            .setVisible(() -> autoSwap.getValue() || antiWeakness.getValue()).build();
    Config<Void> swapConfig = new ConfigGroup.Builder("Swap")
            .addAll(autoSwap, antiWeakness, silentSwap).build();

    private final CrystalBaseScanner baseScanner = new CrystalBaseScanner(10, mc.player);

    public AutoCrystalModule()
    {
        super("AutoCrystal", new String[] {"CrystalAura"}, "Best CA on the market", GuiCategory.COMBAT);
        INSTANCE = this;
    }


}
