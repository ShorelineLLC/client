package net.shoreline.client.impl.module.movement;

import net.minecraft.block.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.eventbus.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.block.BlockSlipperinessEvent;
import net.shoreline.client.impl.event.block.SteppedOnSlimeBlockEvent;
import net.shoreline.client.impl.event.entity.SlowMovementEvent;
import net.shoreline.client.impl.event.entity.VelocityMultiplierEvent;
import net.shoreline.client.impl.event.network.*;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorKeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class NoSlowModule extends ToggleModule {
    public static NoSlowModule INSTANCE;

    //
    Config<Boolean> strictConfig = register(new BooleanConfig("Strict", "Strict NCP bypass for ground slowdowns", false));
    Config<Boolean> airStrictConfig = register(new BooleanConfig("AirStrict", "Strict NCP bypass for air slowdowns", false));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Strict Grim bypass for slowdown", false));
    Config<Boolean> grimNewConfig = register(new BooleanConfig("GrimV3", "Strict GrimV3 bypass for slowdown", false));
    Config<Boolean> strafeFixConfig = register(new BooleanConfig("StrafeFix", "Old NCP bypass for strafe", false));
    Config<Boolean> inventoryMoveConfig = register(new BooleanConfig("InventoryMove", "Allows the player to move while in inventories or screens", true));
    Config<Boolean> arrowMoveConfig = register(new BooleanConfig("ArrowMove", "Allows the player to look while in inventories or screens by using the arrow keys", false));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Removes the slowdown effect caused by using items", true));
    Config<Boolean> shieldsConfig = register(new BooleanConfig("Shields", "Removes the slowdown effect caused by shields", true));
    Config<Boolean> websConfig = register(new BooleanConfig("Webs", "Removes the slowdown caused when moving through webs", false));
    Config<Boolean> berryBushConfig = register(new BooleanConfig("BerryBush", "Removes the slowdown caused when moving through webs", false));
    Config<Float> webSpeedConfig = register(new NumberConfig<>("WebSpeed", "Speed to fall through webs", 0.0f, 3.5f, 20.0f, () -> websConfig.getValue()));
    Config<Boolean> soulsandConfig = register(new BooleanConfig("SoulSand", "Removes the slowdown effect caused by walking over SoulSand blocks", false));
    Config<Boolean> honeyblockConfig = register(new BooleanConfig("HoneyBlock", "Removes the slowdown effect caused by walking over Honey blocks", false));
    Config<Boolean> slimeblockConfig = register(new BooleanConfig("SlimeBlock", "Removes the slowdown effect caused by walking over Slime blocks", false));
    //
    private boolean sneaking;
    //

    /**
     *
     */
    public NoSlowModule() {
        super("NoSlow", "Prevents items from slowing down player", ModuleCategory.MOVEMENT);
        INSTANCE = this;
    }

    public static NoSlowModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        if (airStrictConfig.getValue() && sneaking) {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        sneaking = false;
        Managers.TICK.setClientTick(1.0f);
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event) {
        onEnable();
    }

    @EventListener
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (airStrictConfig.getValue() && !sneaking && checkSlowed()) {
            sneaking = true;
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (event.getStage() == StageEvent.EventStage.PRE && grimConfig.getValue()
                && mc.player.isUsingItem() && !mc.player.isSneaking() && itemsConfig.getValue()) {
            // Grim focuses on other hand noslow checks
            if (mc.player.getActiveHand() == Hand.OFF_HAND && checkStack(mc.player.getMainHandStack())) {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id));
            } else if (checkStack(mc.player.getOffHandStack())) {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
            }
        }
    }

    private boolean checkStack(ItemStack stack) {
        return !stack.isFood() && stack.getItem() != Items.BOW && stack.getItem() != Items.CROSSBOW && stack.getItem() != Items.SHIELD;
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (event.getStage() == StageEvent.EventStage.PRE) {
            if (airStrictConfig.getValue() && sneaking
                    && !mc.player.isUsingItem()) {
                sneaking = false;
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (strafeFixConfig.getValue() && checkSlowed()) {
                // Old NCP
//              Managers.NETWORK.sendSequencedPacket(id ->
//                      new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
//                              new BlockHitResult(mc.player.getPos(), Direction.UP, mc.player.getBlockPos(), false), id));
            }
            if (inventoryMoveConfig.getValue() && checkScreen()) {
                final long handle = mc.getWindow().getHandle();
                KeyBinding[] keys = new KeyBinding[]{mc.options.jumpKey, mc.options.forwardKey, mc.options.backKey, mc.options.rightKey, mc.options.leftKey};
                for (KeyBinding binding : keys) {
                    binding.setPressed(InputUtil.isKeyPressed(handle, ((AccessorKeyBinding) binding).getBoundKey().getCode()));
                }
                if (arrowMoveConfig.getValue()) {
                    float yaw = mc.player.getYaw();
                    float pitch = mc.player.getPitch();
                    if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_UP)) {
                        pitch -= 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_DOWN)) {
                        pitch += 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT)) {
                        yaw -= 3.0f;
                    } else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT)) {
                        yaw += 3.0f;
                    }
                    mc.player.setYaw(yaw);
                    mc.player.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f));
                }
            }
            if (grimConfig.getValue() && (websConfig.getValue() || berryBushConfig.getValue())) {
                for (BlockPos pos : getIntersectingWebs()) {
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
                    Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
                }
            }
        }
    }

    @EventListener
    public void onStrafeFix(StrafeFixEvent event) {
        if (strafeFixConfig.getValue()) {
            float yaw = Managers.ROTATION.getServerYaw();
            float pitch = Managers.ROTATION.getServerPitch();
            if (Managers.ROTATION.isRotating())
            {
                yaw = Managers.ROTATION.getRotationYaw();
                pitch = Managers.ROTATION.getRotationPitch();
            }
            event.cancel();
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @EventListener
    public void onSlowMovement(SlowMovementEvent event) {
        Block block = event.getState().getBlock();
        if (block instanceof CobwebBlock && websConfig.getValue()
                || block instanceof SweetBerryBushBlock && berryBushConfig.getValue()) {
            if (grimConfig.getValue()) {
                event.cancel();
            } else if (mc.player.isOnGround()) {
                Managers.TICK.setClientTick(1.0f);
            } else {
                Managers.TICK.setClientTick(webSpeedConfig.getValue() / 2.0f);
            }
        }
    }

    @EventListener
    public void onMovementSlowdown(MovementSlowdownEvent event) {
        if (checkSlowed()) {
            event.input.movementForward *= 5.0f;
            event.input.movementSideways *= 5.0f;
        }
    }

    @EventListener
    public void onVelocityMultiplier(VelocityMultiplierEvent event) {
        if (event.getBlock() == Blocks.SOUL_SAND && soulsandConfig.getValue()
                || event.getBlock() == Blocks.HONEY_BLOCK && honeyblockConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onSteppedOnSlimeBlock(SteppedOnSlimeBlockEvent event) {
        if (slimeblockConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onBlockSlipperiness(BlockSlipperinessEvent event) {
        if (event.getBlock() == Blocks.SLIME_BLOCK
                && slimeblockConfig.getValue()) {
            event.cancel();
            event.setSlipperiness(0.6f);
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (mc.player == null || mc.world == null || mc.isInSingleplayer()) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesPosition()
                && strictConfig.getValue() && checkSlowed()) {
            // Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(0));
            // Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id));
            Managers.INVENTORY.setSlotForced(mc.player.getInventory().selectedSlot);
        } else if (event.getPacket() instanceof ClickSlotC2SPacket && strictConfig.getValue()) {
            if (mc.player.isUsingItem()) {
                mc.player.stopUsingItem();
            }
            if (sneaking || Managers.POSITION.isSneaking()) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (Managers.POSITION.isSprinting()) {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                        ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        } else if (event.getPacket() instanceof PlayerInteractItemC2SPacket && grimNewConfig.getValue()) {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN, id));
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN, id));
        }
    }

    public boolean checkSlowed() {
//        ItemStack offHandStack = mc.player.getOffHandStack();
//        if ((offHandStack.isFood() || offHandStack.getItem() == Items.BOW || offHandStack.getItem() == Items.CROSSBOW || offHandStack.getItem() == Items.SHIELD) && grimConfig.getValue()) {
//            return false;
//        }
        return !mc.player.isRiding() && !mc.player.isSneaking() && (mc.player.isUsingItem() && itemsConfig.getValue() || mc.player.isBlocking() && shieldsConfig.getValue() && !grimConfig.getValue());
    }

    public boolean checkScreen() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof SignEditScreen || mc.currentScreen instanceof DeathScreen);
    }

    public List<BlockPos> getIntersectingWebs() {
        int radius = 5;
        final List<BlockPos> blocks = new ArrayList<>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    BlockPos blockPos = BlockPos.ofFloored(mc.player.getX() + x,
                            mc.player.getY() + y, mc.player.getZ() + z);
                    BlockState state = mc.world.getBlockState(blockPos);
                    if (state.getBlock() instanceof CobwebBlock && websConfig.getValue()
                            || state.getBlock() instanceof SweetBerryBushBlock && berryBushConfig.getValue()) {
                        blocks.add(blockPos);
                    }
                }
            }
        }
        return blocks;
    }

    public boolean getStrafeFix() {
        return strafeFixConfig.getValue();
    }
}
