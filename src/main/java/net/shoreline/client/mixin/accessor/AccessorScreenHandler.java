package net.shoreline.client.mixin.accessor;

import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author xgraza
 * @since 05/18/24
 */
@Mixin(ScreenHandler.class)
public interface AccessorScreenHandler
{
    @Accessor("revision")
    void hookSetRevision(final int revisionID);
}
