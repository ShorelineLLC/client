package net.shoreline.client.impl.module.misc;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.gui.hud.OpenTabEvent;
import net.shoreline.client.impl.event.gui.hud.RenderPlayerListEvent;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
public class BetterTabModule extends Toggleable
{
    public static BetterTabModule INSTANCE;

    Config<Boolean> animateConfig = new BooleanConfig.Builder("Animate")
            .setDescription("Animates the tab list")
            .setDefaultValue(false).build();
    Config<Integer> playerLimit = new NumberConfig.Builder<Integer>("Limit")
            .setMin(80).setMax(1000).setDefaultValue(80)
            .setDescription("The max number of players shown in tab list").build();
    Config<Integer> playerColumns = new NumberConfig.Builder<Integer>("Columns")
            .setMin(20).setMax(100).setDefaultValue(20)
            .setDescription("The number of columns to show in tab list").build();

    private final Animation tabListAnim = new Animation(200L);

    public BetterTabModule()
    {
        super("BetterTab", "Improves server tab list", GuiCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    @EventListener
    public void onRenderPlayerListPre(RenderPlayerListEvent.Pre event)
    {
        if (animateConfig.getValue())
        {
            float animFactor = (float) Easing.SMOOTH_STEP.ease(tabListAnim.getFactor());
            int h = event.getY2() - event.getY1();
            int cb = event.getY1() + Math.round(h * animFactor);
            event.getContext().enableScissor(event.getX1(), event.getY1(), event.getX2(), Math.max(event.getY1(), Math.min(event.getY2(), cb)));
        }
    }

    @EventListener
    public void onRenderPlayerListPost(RenderPlayerListEvent.Post event)
    {
        if (animateConfig.getValue())
        {
            event.getContext().disableScissor();
        }
    }

    @EventListener
    public void onRenderPlayerListText(RenderPlayerListEvent.DrawText event)
    {
        String[] tabEntry = event.getText().getString().split(" ");
        for (String s : tabEntry)
        {
            if (s.equals(mc.getGameProfile().getName()))
            {
                event.cancel();
                event.setColor(ThemeModule.INSTANCE.getPrimaryColor().getRGB());
                return;
            } else if (Managers.SOCIAL.isFriend(s))
            {
                event.cancel();
                event.setColor(SocialsModule.INSTANCE.getFriendsColor().getRGB());
                return;
            }
        }
    }

    @EventListener
    public void onScreenOpen(OpenTabEvent event)
    {
        if (event.isVisible())
        {
            tabListAnim.setState(true);
        } else
        {
            tabListAnim.setStateHard(false);
        }
    }
}
