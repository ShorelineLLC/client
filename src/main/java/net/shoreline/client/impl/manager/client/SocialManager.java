package net.shoreline.client.impl.manager.client;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.shoreline.client.api.social.SocialRelation;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages client social relationships by storing the associated player
 * {@link UUID}'s with a {@link SocialRelation} value to the user. Backed by a
 * {@link ConcurrentMap} so getting/setting runs in O(1).
 *
 * @author linus
 * @see UUID
 * @see SocialRelation
 * @since 1.0
 */
public class SocialManager implements Globals
{
    //
    private final ConcurrentMap<String, SocialRelation> relationships =
            new ConcurrentHashMap<>();

    /**
     * @param name
     * @param relation
     * @return
     */
    public boolean isRelation(String name, SocialRelation relation)
    {
        return relationships.get(name) == relation;
    }

    /**
     * @param name
     * @return
     * @see #isRelation(String, SocialRelation)
     */
    public boolean isFriend(String name)
    {
        if (!SocialsModule.getInstance().isFriendsEnabled())
        {
            return false;
        }
        return isRelation(name, SocialRelation.FRIEND);
    }

    public boolean isFriendInternal(String name)
    {
        return isRelation(name, SocialRelation.FRIEND);
    }

    public boolean isFriend(Text name)
    {
        if (!SocialsModule.getInstance().isFriendsEnabled())
        {
            return false;
        }
        return name != null && isRelation(name.getString(), SocialRelation.FRIEND);
    }

    /**
     * @param name
     * @param relation
     */
    public void addRelation(String name, SocialRelation relation)
    {
        if (mc.player != null && name.equals(mc.player.getDisplayName().getString()))
        {
            return;
        }
        final SocialRelation relationship = relationships.get(name);
        if (relationship != null)
        {
            relationships.replace(name, relation);
            return;
        }
        relationships.put(name, relation);
    }

    /**
     * @param name
     * @see #addRelation(String, SocialRelation)
     */
    public void addFriend(String name)
    {
        addRelation(name, SocialRelation.FRIEND);
        if (SocialsModule.getInstance().shouldNotify() && mc.world != null)
        {
            Collection<PlayerListEntry> playerListEntries = mc.player.networkHandler.getPlayerList();
            for (PlayerListEntry playerListEntry : playerListEntries)
            {
                String playerName = playerListEntry.getProfile().getName();
                String[] names = playerName.split(" ");
                for (String name1 : names)
                {
                    if (name1.equals(name))
                    {
                        ChatUtil.serverSendCommand("msg " + name + " I just added you as a friend on Shoreline!");
                        break;
                    }
                }
            }
        }
    }

    public void addFriend(Text name)
    {
        addFriend(name.getString());
    }

    public SocialRelation remove(String playerName)
    {
        return relationships.remove(playerName);
    }

    public SocialRelation remove(Text playerName)
    {
        return relationships.remove(playerName.getString());
    }

    /**
     * @param relation
     * @return
     */
    public Collection<String> getRelations(SocialRelation relation)
    {
        final List<String> friends = new ArrayList<>();
        for (Map.Entry<String, SocialRelation> relationship :
                relationships.entrySet())
        {
            if (relationship.getValue() == relation)
            {
                friends.add(relationship.getKey());
            }
        }
        return friends;
    }

    /**
     * @return
     * @see #getRelations(SocialRelation)
     */
    public Collection<String> getFriends()
    {
        return getRelations(SocialRelation.FRIEND);
    }
}
