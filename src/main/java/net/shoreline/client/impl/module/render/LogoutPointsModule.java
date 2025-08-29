package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class LogoutPointsModule extends Toggleable
{

    public LogoutPointsModule()
    {
        super("LogoutPoints", new String[] {"LogoutSpots"}, "Marks nearby logouts", GuiCategory.RENDER);
    }

    
}
