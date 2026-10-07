package net.shoreline.client.util.item;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

@UtilityClass
public class ArmorUtil
{
    public double getArmorValue(ItemStack stack)
    {
        double armorValue = 0.0;
        if (!stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS))
        {
            return 0.0;
        }

        AttributeModifiersComponent component = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        for (AttributeModifiersComponent.Entry modifier : component.modifiers())
        {
            if (modifier.attribute() == EntityAttributes.ARMOR || modifier.attribute() == EntityAttributes.ARMOR_TOUGHNESS)
            {
                double e = modifier.modifier().value();

                armorValue += switch (modifier.modifier().operation())
                {
                    case ADD_VALUE -> e;
                    case ADD_MULTIPLIED_BASE -> e * MinecraftClient.getInstance().player.getAttributeBaseValue(modifier.attribute());
                    case ADD_MULTIPLIED_TOTAL -> e * armorValue;
                };
            }
        }

        return armorValue;
    }
}
