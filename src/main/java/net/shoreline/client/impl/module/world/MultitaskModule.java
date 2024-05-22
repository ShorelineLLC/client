package net.shoreline.client.impl.module.world;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.ItemMultitaskEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;

/**
 * @author linus
 * @since 1.0
 */
public class MultitaskModule extends ToggleModule {

    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Allows you to eat while placing on grim", false));

    public MultitaskModule() {
        super("MultiTask", "Allows you to mine and use items simultaneously", ModuleCategory.WORLD);
    }

    @EventListener
    public void onItemMultitask(ItemMultitaskEvent event) {
        if (!grimConfig.getValue()) {
            event.cancel();
        }
    }
}
