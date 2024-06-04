package net.shoreline.client.util.string;

import net.minecraft.util.math.Direction;

/**
 * @author bon55, linus
 * @since 1.0
 */
public class EnumFormatter
{
    /**
     * Formats an enum
     *
     * @param in The enum to format
     * @return The formatted enum
     */
    public static String formatEnum(final Enum<?> in)
    {
        String name = in.name();
        // no capitalization
        if (!name.contains("_"))
        {
            char firstChar = name.charAt(0);
            String suffixChars = name.split(String.valueOf(firstChar), 2)[1];
            return String.valueOf(firstChar).toUpperCase() + suffixChars.toLowerCase();
        }
        String[] names = name.split("_");
        StringBuilder nameToReturn = new StringBuilder();
        for (String n : names)
        {
            char firstChar = n.charAt(0);
            String suffixChars = n.split(String.valueOf(firstChar), 2)[1];
            nameToReturn.append(String.valueOf(firstChar).toUpperCase())
                    .append(suffixChars.toLowerCase());
        }
        return nameToReturn.toString();
    }

    /**
     * @param direction
     * @return
     */
    public static String formatDirection(Direction direction)
    {
        return switch (direction)
        {
            case UP -> "Up";
            case DOWN -> "Down";
            case NORTH -> "North";
            case SOUTH -> "South";
            case EAST -> "East";
            case WEST -> "West";
        };
    }

    /**
     * @param axis
     * @return
     */
    public static String formatAxis(Direction.Axis axis)
    {
        return switch (axis)
        {
            case X -> "X";
            case Y -> "Y";
            case Z -> "Z";
        };
    }
}
