package net.shoreline.client.impl.module.movement;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.SlowMovementEvent;
import net.shoreline.client.impl.event.network.MovementFactorEvent;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.imixin.IKeyBinding;
import net.shoreline.eventbus.annotation.EventListener;

public class NoSlowModule extends Toggleable
{
    Config<ItemMode> itemMode = new EnumConfig.Builder<ItemMode>("Item")
            .setValues(ItemMode.values())
            .setDescription("Removes the slowdown from consuming items")
            .setDefaultValue(ItemMode.NORMAL).build();
    Config<Boolean> blockingConfig = new BooleanConfig.Builder("Blocking")
            .setDescription("Removes the slowdown from blocking with a shield")
            .setDefaultValue(false).build();
    Config<Boolean> sneakingConfig = new BooleanConfig.Builder("Sneaking")
            .setDescription("Removes the slowdown from sneaking")
            .setDefaultValue(false).build();
    Config<Boolean> crawlingConfig = new BooleanConfig.Builder("Crawling")
            .setDescription("Removes the slowdown from crawling")
            .setDefaultValue(false).build();
    Config<Boolean> soulSandConfig = new BooleanConfig.Builder("SoulSand")
            .setDescription("Removes the slowdown when walking on soul sand")
            .setDefaultValue(false).build();
    Config<Boolean> honeyConfig = new BooleanConfig.Builder("Honey")
            .setDescription("Removes the slowdown when walking on honey")
            .setDefaultValue(false).build();
    Config<Boolean> inventoryMoveConfig = new BooleanConfig.Builder("InventoryMove")
            .setDescription("Allows the player to move while inventories or menus are open")
            .setDefaultValue(true).build();

    public NoSlowModule()
    {
        super("NoSlow", new String[] {"NoSlowdown"}, "Prevents client from slowing the player", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTickPre(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        if (inventoryMoveConfig.getValue() && mc.currentScreen != null
                && !(mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof SignEditScreen
                || mc.currentScreen instanceof DeathScreen))
        {
            final long handle = mc.getWindow().getHandle();
            KeyBinding[] keys = new KeyBinding[] { mc.options.jumpKey, mc.options.forwardKey, mc.options.backKey, mc.options.rightKey, mc.options.leftKey };
            for (KeyBinding binding : keys)
            {
                binding.setPressed(InputUtil.isKeyPressed(handle, ((IKeyBinding) binding).getBoundKey().getCode()));
            }
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (itemMode.getValue() == ItemMode.GRIM_V2 && shouldCancelSlowedDown())
        {
            if (mc.player.getActiveHand() == Hand.OFF_HAND && !canUseItem(mc.player.getMainHandStack()))
            {
                sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            } else if (!canUseItem(mc.player.getOffHandStack()))
            {
                sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            }
        }
    }

    @EventListener
    public void onItemSlowdown(MovementFactorEvent.Item event)
    {
        if (shouldCancelSlowedDown())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onSlowdown(MovementFactorEvent.Slowdown event)
    {
        if (sneakingConfig.getValue() && mc.player.isSneaking()
                || crawlingConfig.getValue() && mc.player.isCrawling())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onSlowMovement(SlowMovementEvent.Block event)
    {
        if (soulSandConfig.getValue() && event.getBlock() == Blocks.SOUL_SAND
                || honeyConfig.getValue() && event.getBlock() == Blocks.HONEY_BLOCK)
        {
            event.cancel();
        }
    }

    private boolean canUseItem(ItemStack stack)
    {
        return stack.getComponents().contains(DataComponentTypes.FOOD) || stack.getItem() == Items.BOW || stack.getItem() == Items.CROSSBOW || stack.getItem() == Items.SHIELD || stack.getItem() == Items.ENDER_PEARL;
    }

    public boolean shouldCancelSlowedDown()
    {
        if (itemMode.getValue() == ItemMode.GRIM_V3 && !canBypassGrimUseTime())
        {
            return false;
        }

        return mc.player.isUsingItem() && itemMode.getValue() != ItemMode.OFF
                || mc.player.isBlocking() && blockingConfig.getValue();
    }

    private boolean canBypassGrimUseTime()
    {
        return mc.player.getItemUseTimeLeft() < 5 || (mc.player.getItemUseTime() > 1 && mc.player.getItemUseTime() % 2 != 0);
    }

    private enum ItemMode
    {
        NORMAL,
        STRICT,
        GRIM_V2,
        GRIM_V3,
        OFF
    }
}
