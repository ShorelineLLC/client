package net.shoreline.client.impl.social;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.module.client.SocialsModule;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SocialManager extends GenericFeature
{
    private final Map<String, SocialType> socials = new HashMap<>();

    public SocialManager()
    {
        super("Socials", new String[] { "Friends", "Enemies" });
    }

    /* ------------- Friends ------------- */
    public void addFriend(String friendName)
    {
        addSocial(friendName, SocialType.FRIEND);
    }

    public void toggleFriend(String friendName)
    {
        if (isFriend(friendName))
        {
            removeSocial(friendName);
        } else
        {
            addFriend(friendName);
        }
    }

    public boolean isFriend(Entity entity)
    {
        return SocialsModule.INSTANCE.getFriendsConfig().getValue() && isFriendInternal(entity.getName().getString());
    }

    public boolean isFriend(String friendName)
    {
        return SocialsModule.INSTANCE.getFriendsConfig().getValue() && isFriendInternal(friendName);
    }

    public boolean isFriendInternal(String friendName)
    {
        return isType(friendName, SocialType.FRIEND);
    }

    /* ------------- Enemies ------------- */
    public void addEnemy(String enemyName)
    {
        socials.put(enemyName, SocialType.FRIEND);
    }

    public void removeEnemy(String enemyName)
    {
        socials.remove(enemyName);
    }

    public boolean isEnemy(Entity entity)
    {
        return SocialsModule.INSTANCE.getFriendsConfig().getValue() && isEnemyInternal(entity.getName().getString());
    }

    public boolean isEnemy(String enemyName)
    {
        return SocialsModule.INSTANCE.getFriendsConfig().getValue() && isEnemyInternal(enemyName);
    }

    public boolean isEnemyInternal(String enemyName)
    {
        return isType(enemyName, SocialType.ENEMY);
    }

    /* ------------- Util ------------- */
    public SocialType getType(String name)
    {
        return socials.get(name);
    }

    public boolean isType(String name, SocialType type)
    {
        return socials.get(name) == type;
    }

    public void addSocial(String name, SocialType type)
    {
        socials.put(name, type);
    }

    public void removeSocial(String name)
    {
        socials.remove(name);
    }

    public Set<String> getTypes(SocialType type)
    {
        return socials.entrySet().stream().filter(entry -> entry.getValue() == type)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public enum SocialType
    {
        FRIEND,
        ENEMY
    }
}
