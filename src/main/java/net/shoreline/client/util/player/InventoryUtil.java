package net.shoreline.client.util.player;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.shoreline.client.util.Globals;

/**
 * @author linus
 * @since 1.0
 */
public class InventoryUtil implements Globals
{
    /**
     * @return
     */
    public static boolean isHolding32k()
    {
        return isHolding32k(1000);
    }

    /**
     * @param lvl
     * @return
     */
    public static boolean isHolding32k(int lvl)
    {
        final ItemStack mainhand = mc.player.getMainHandStack();
        return EnchantmentUtil.getLevel(mainhand, Enchantments.SHARPNESS) >= lvl;
    }

    public static boolean hasItemInInventory(final Item item, final boolean hotbar)
    {
        final int startSlot = hotbar ? 0 : 9;
        for (int i = startSlot; i < 36; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() == item)
            {
                return true;
            }
        }
        return false;
    }
}
