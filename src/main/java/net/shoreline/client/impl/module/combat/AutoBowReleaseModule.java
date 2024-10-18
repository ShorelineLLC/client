package net.shoreline.client.impl.module.combat;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class AutoBowReleaseModule extends ToggleModule
{
    //
    Config<Boolean> crossbowConfig = register(new BooleanConfig("Crossbow", "Automatically releases crossbow when fully charged", false));
    Config<Integer> ticksConfig = register(new NumberConfig<>("Ticks", "Ticks before releasing the bow charge", 3, 5, 20));
    Config<Boolean> tpsSyncConfig = register(new BooleanConfig("TPS-Sync", "Sync bow release to server ticks", false));

    /**
     *
     */
    public AutoBowReleaseModule()
    {
        super("AutoBowRelease", "Automatically releases a charged bow",
                ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (SelfBowModule.getInstance().isEnabled())
        {
            return;
        }
        if (event.getStage() == StageEvent.EventStage.POST)
        {
            ItemStack mainhand = mc.player.getMainHandStack();
            if (mainhand.getItem() == Items.BOW)
            {
                float off = tpsSyncConfig.getValue() ? 20.0f - Managers.TICK.getTpsAverage() : 0.0f;
                if (mc.player.getItemUseTime() + off >= ticksConfig.getValue())
                {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN, Direction.DOWN));
                    // Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
                    mc.player.stopUsingItem();
                }
            }
            else if (crossbowConfig.getValue() && mainhand.getItem() == Items.CROSSBOW
                    && mc.player.getItemUseTime() / (float) CrossbowItem.getPullTime(mc.player.getMainHandStack(), mc.player) > 1.0f)
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN, Direction.DOWN));
                mc.player.stopUsingItem();
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        }
    }
}
