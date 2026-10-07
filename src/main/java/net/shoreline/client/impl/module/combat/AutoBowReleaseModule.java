package net.shoreline.client.impl.module.combat;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoBowReleaseModule extends Toggleable
{
    Config<Boolean> crossbowConfig = new BooleanConfig.Builder("Crossbow")
            .setDescription("Automatically releases crossbow when fully charged")
            .setDefaultValue(false).build();
    Config<Integer> ticksConfig = new NumberConfig.Builder<Integer>("Ticks")
            .setMin(3).setMax(20).setDefaultValue(5)
            .setDescription("Ticks before releasing the bow charge").build();

    public AutoBowReleaseModule()
    {
        super("AutoBowRelease", new String[] {"AutoBow", "FastBow"},
                "Automatically releases a charged bow", GuiCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        ItemStack mainhand = mc.player.getMainHandStack();
        if (mainhand.getItem() == Items.BOW)
        {
            if (mc.player.getItemUseTime() >= ticksConfig.getValue())
            {
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                // sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
                mc.player.stopUsingItem();
            }
        }
        else if (crossbowConfig.getValue() && mainhand.getItem() == Items.CROSSBOW
                && mc.player.getItemUseTime() / (float) CrossbowItem.getPullTime(mc.player.getMainHandStack(), mc.player) > 1.0f)
        {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.stopUsingItem();
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
