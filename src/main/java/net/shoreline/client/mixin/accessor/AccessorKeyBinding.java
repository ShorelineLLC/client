package net.shoreline.client.mixin.accessor;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding
{
    /**
     * @return
     */
    @Accessor("boundKey")
    InputUtil.Key getBoundKey();
}
