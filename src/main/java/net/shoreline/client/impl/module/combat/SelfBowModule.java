package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;

import java.util.HashSet;
import java.util.Set;

/**
 * @author linus
 * @since 1.0
 */
public class SelfBowModule extends RotationModule {
    private static SelfBowModule INSTANCE;

    //
    private final Set<StatusEffectInstance> arrows = new HashSet<>();

    /**
     *
     */
    public SelfBowModule() {
        super("SelfBow", "Shoots player with beneficial tipped arrows", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static SelfBowModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(false);
        arrows.clear();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        int arrowSlot = -1;
        StatusEffectInstance statusEffect = null;
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof TippedArrowItem)) {
                continue;
            }
            Potion p = PotionUtil.getPotion(stack);
            for (StatusEffectInstance effect : p.getEffects()) {
                StatusEffect type = effect.getEffectType();
                if (type.isBeneficial() && !arrows.contains(effect)) {
                    arrowSlot = i;
                    statusEffect = effect;
                    break;
                }
            }
            if (arrowSlot != -1) {
                break;
            }
        }
        int bowSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.BOW) {
                bowSlot = i;
                break;
            }
        }
        if (mc.player.getMainHandStack().getItem() != Items.BOW || bowSlot == -1 || arrowSlot == -1) {
            disable();
            return;
        }
        setRotation(mc.player.getYaw(), -90.0f);
        mc.interactionManager.clickSlot(0, arrowSlot, 9, SlotActionType.SWAP, mc.player);
        float pullTime = BowItem.getPullProgress(mc.player.getItemUseTime());
        if (pullTime >= 0.15f) {
            arrows.add(statusEffect);
            mc.options.useKey.setPressed(false);
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.stopUsingItem();
        } else {
            mc.options.useKey.setPressed(true);
        }
    }
}
