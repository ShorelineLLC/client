package net.shoreline.client.impl.combat;

import lombok.Getter;
import net.minecraft.inventory.Inventory;
import net.shoreline.client.api.LoggingFeature;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KitManager extends LoggingFeature
{
    private final Map<String, PvpKit> pvpKits = new HashMap<>();

    @Getter
    private String currentKit;

    public KitManager()
    {
        super("PVP Kit");
    }

    public void setCurrentKit(String name)
    {
        if (!pvpKits.containsKey(name))
        {
            return;
        }

        currentKit = name;
    }

    public void saveKit(String name, Inventory inventory)
    {
        pvpKits.put(name, new PvpKit(name, inventory));
    }

    public void saveKit(String name, PvpKit kit)
    {
        pvpKits.put(name, kit);
    }

    public void deleteKit(String name)
    {
        pvpKits.remove(name);
    }

    public Set<String> getKitNames()
    {
        return pvpKits.keySet();
    }

    public Collection<PvpKit> getKits()
    {
        return pvpKits.values();
    }
}
