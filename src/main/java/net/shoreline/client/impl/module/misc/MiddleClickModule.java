package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.InputEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.util.world.RaytraceUtil;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

public class MiddleClickModule extends Toggleable
{
    Config<Boolean> pearlConfig = new BooleanConfig.Builder("Pearl")
            .setDescription("Middle click to throw a pearl")
            .setDefaultValue(true).build();
    Config<Boolean> friendConfig = new BooleanConfig.Builder("Friend")
            .setDescription("Middle click to friend a player")
            .setDefaultValue(false).build();
    Config<Boolean> fireworkConfig = new BooleanConfig.Builder("Firework")
            .setDescription("Middle click to boost an elytra using a firework")
            .setDefaultValue(false).build();

    public MiddleClickModule()
    {
        super("MiddleClick", "Middle click to perform an action", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onInput(InputEvent.Mouse event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && event.getAction() == GLFW.GLFW_PRESS)
        {
            HitResult raytrace = RaytraceUtil.raycastFromCamera(mc.player.getEntityInteractionRange());
            if (raytrace != null && raytrace.getType() == HitResult.Type.ENTITY)
            {
                EntityHitResult hitResult = (EntityHitResult) raytrace;
                if (hitResult.getEntity() instanceof PlayerEntity player && friendConfig.getValue())
                {
                    Managers.SOCIAL.toggleFriend(player.getName().getString());
                    return;
                }
            }

            if (mc.player.isGliding() && fireworkConfig.getValue())
            {
                useItem(Items.FIREWORK_ROCKET);
                return;
            }

            if (pearlConfig.getValue())
            {
                useItem(Items.ENDER_PEARL);
            }
        }
    }

    private void useItem(Item item)
    {
        int slot = InventoryUtil.getItemSlot(item);
        if (slot == -1 || !Managers.INVENTORY.startSwap(slot))
        {
            return;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        Managers.INVENTORY.endSwap();
    }
}
