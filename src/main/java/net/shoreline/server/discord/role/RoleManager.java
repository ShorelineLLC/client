package net.shoreline.server.discord.role;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.shoreline.server.discord.DiscordBot;

public final class RoleManager
{
    public static boolean addRole(String username,
                                  String roleName)
    {
        Role role = DiscordBot.SHORELINE_GUILD.getRolesByName(roleName, true)
                .stream()
                .findFirst()
                .orElse(null);

        if (role == null)
        {
            return false;
        }

        Member member = DiscordBot.SHORELINE_GUILD.loadMembers()
                .get()
                .stream()
                .filter(m -> m.getUser().getName().equals(username))
                .findFirst()
                .orElse(null);

        if (member == null)
        {
            return false;
        }

        try
        {
            DiscordBot.SHORELINE_GUILD.addRoleToMember(member, role).complete();
            return true;
        } catch (Throwable t)
        {
            return false;
        }
    }
}
