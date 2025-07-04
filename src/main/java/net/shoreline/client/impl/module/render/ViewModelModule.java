package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ViewModelModule extends Toggleable
{

    public ViewModelModule()
    {
        super("ViewModel", "Changes the hand viewmodel", GuiCategory.RENDER);
    }
}
