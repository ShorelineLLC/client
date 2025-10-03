package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class BreadcrumbsModule extends Toggleable
{

    public BreadcrumbsModule()
    {
        super("Breadcrumbs", "Shows entity trails", GuiCategory.RENDER);
    }
}
