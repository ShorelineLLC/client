package net.shoreline.client.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author linus
 * @see MinecraftClient
 * @since 1.0
 */
@Mixin(MinecraftClient.class)
public interface AccessorMinecraftClient
{
    /**
     * @param itemUseCooldown
     */
    @Accessor("itemUseCooldown")
    void hookSetItemUseCooldown(int itemUseCooldown);

    /**
     * @return
     */
    @Accessor("itemUseCooldown")
    int hookGetItemUseCooldown();

    /**
     * @param attackCooldown
     */
    @Accessor("attackCooldown")
    void hookSetAttackCooldown(int attackCooldown);

    @Invoker("doItemUse")
    void hookDoItemUse();

    @Accessor("session")
    @Final
    @Mutable
    void setSession(Session session);

    @Accessor("world")
    void hookSetWorld(ClientWorld world);

    @Accessor("disconnecting")
    void hookSetDisconnecting(boolean disconnecting);
}
