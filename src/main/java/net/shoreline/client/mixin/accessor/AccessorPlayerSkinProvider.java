package net.shoreline.client.mixin.accessor;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(PlayerSkinProvider.class)
public interface AccessorPlayerSkinProvider
{
    /**
     * @return
     */
    @Accessor("skinCache")
    PlayerSkinProvider.FileCache getSkinCacheDir();
}
