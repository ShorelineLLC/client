package net.shoreline.client.impl.imixin;

import net.minecraft.client.util.InputUtil;

@IMixin
public interface IKeyBinding
{
    InputUtil.Key getBoundKey();
}
