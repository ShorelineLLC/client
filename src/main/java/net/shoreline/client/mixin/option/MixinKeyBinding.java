package net.shoreline.client.mixin.option;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.shoreline.client.impl.imixin.IKeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding implements IKeyBinding
{
    @Override
    @Accessor("boundKey")
    public abstract InputUtil.Key getBoundKey();
}
