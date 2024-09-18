package net.shoreline.client.mixin.accessor;

import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(GameOptions.class)
public interface AccessorGameOptions
{
    /**
     * @return
     */
    @Accessor("enabledPlayerModelParts")
    @Mutable
    Set<PlayerModelPart> getPlayerModelParts();
}
