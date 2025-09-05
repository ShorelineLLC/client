package net.shoreline.client.impl.social;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.module.client.SocialsModule;

import java.util.HashSet;
import java.util.Set;

@Getter
public class SocialManager extends GenericFeature
{
    private final Set<String> friends = new HashSet<>();

    public SocialManager()
    {
        super("Socials", new String[] { "Friends", "Enemies" });
    }

    public void addFriend(String friendName)
    {
        friends.add(friendName);
    }

    public void removeFriend(String friendName)
    {
        friends.remove(friendName);
    }

    public boolean isFriend(Entity entity)
    {
        return SocialsModule.INSTANCE.getFriendsConfig().getValue() && isFriend(entity.getName().getString());
    }

    public boolean isFriend(String friendName)
    {
        return friends.contains(friendName);
    }
}
