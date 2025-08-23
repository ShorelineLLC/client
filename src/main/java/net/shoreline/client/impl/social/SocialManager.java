package net.shoreline.client.impl.social;

import net.shoreline.client.api.GenericFeature;

import java.util.ArrayList;
import java.util.List;

public class SocialManager extends GenericFeature
{
    private final List<String> friends = new ArrayList<>();

    public SocialManager()
    {
        super("Socials", new String[] { "Friends", "Enemies" });
    }


}
