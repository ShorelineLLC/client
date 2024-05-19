package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.MouseClickEvent;
import net.shoreline.client.impl.module.render.FreecamModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.RayCastUtil;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @since 1.0
 */
public class MiddleClickModule extends ToggleModule {

    //
    Config<Boolean> friendConfig = new BooleanConfig("Friend", "Friends players when middle click", true);
    Config<Boolean> pearlConfig = new BooleanConfig("Pearl", "Throws a pearl when middle click", true);
    Config<Boolean> fireworkConfig = new BooleanConfig("Firework", "Uses firework to boost elytra when middle click", false);

    /**
     *
     */
    public MiddleClickModule() {
        super("MiddleClick", "Adds an additional bind on the mouse middle button",
                ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onMouseClick(MouseClickEvent event) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == GLFW.GLFW_PRESS && mc.currentScreen == null) {
            double d = mc.interactionManager.hasExtendedReach() ? 6.0 : mc.interactionManager.getReachDistance();
            HitResult result = FreecamModule.INSTANCE.isEnabled() ? RayCastUtil.raycastEntity(d, FreecamModule.INSTANCE.getCameraPosition(), FreecamModule.INSTANCE.getCameraRotations()) : RayCastUtil.raycastEntity(d);
            if (friendConfig.getValue() && result != null && result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() instanceof PlayerEntity target) {
                if (Managers.SOCIAL.isFriend(target.getName())) {
                    Managers.SOCIAL.remove(target.getName());
                } else {
                    Managers.SOCIAL.addFriend(target.getName());
                }
            } else {
                Item item = null;
                if (mc.player.isFallFlying() && fireworkConfig.getValue()) {
                    item = Items.FIREWORK_ROCKET;
                } else if (pearlConfig.getValue()) {
                    item = Items.ENDER_PEARL;
                }
                if (item == null) {
                    return;
                }
                int slot = -1;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack.getItem() == item) {
                        slot = i;
                        break;
                    }
                }
                if (slot != -1) {
                    int prev = mc.player.getInventory().selectedSlot;
                    Managers.INVENTORY.setClientSlot(slot);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    Managers.INVENTORY.setClientSlot(prev);
                }
            }
        }
    }
}
