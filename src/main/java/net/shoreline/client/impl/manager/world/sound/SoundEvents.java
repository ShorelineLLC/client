package net.shoreline.client.impl.manager.world.sound;

import net.minecraft.util.Identifier;

/**
 * @author linus
 * @since 1.0
 */
public enum SoundEvents
{
    CLICK("gui_click");

    //
    private final Identifier id;

    /**
     * @param id
     */
    SoundEvents(String id)
    {
        this.id = new Identifier("caspian", String.format("sounds/%s.ogg", id));
    }

    /**
     * @return
     */
    public Identifier getId()
    {
        return id;
    }
}
