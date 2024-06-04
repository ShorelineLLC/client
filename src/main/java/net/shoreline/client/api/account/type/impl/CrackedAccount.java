package net.shoreline.client.api.account.type.impl;

import net.minecraft.client.session.Session;
import net.shoreline.client.api.account.type.MinecraftAccount;

import java.util.Optional;
import java.util.UUID;

/**
 * @author xgraza
 * @since 03/31/24
 */
public record CrackedAccount(String username) implements MinecraftAccount
{
    @Override
    public Session login()
    {
        return new Session(username(),
                UUID.randomUUID(), "", Optional.empty(),
                Optional.empty(), Session.AccountType.LEGACY);
    }
}
