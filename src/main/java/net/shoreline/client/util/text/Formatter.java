package net.shoreline.client.util.text;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Formatter
{
    public String capitalize(String string)
    {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public String formatEnum(final Enum<?> in)
    {
        String name = in.name();
        if (name.equalsIgnoreCase("KMH") || name.equalsIgnoreCase("BPS") || name.equalsIgnoreCase("NCP") || name.equalsIgnoreCase("XQZ"))
        {
            return name.toUpperCase();
        } else if (name.equalsIgnoreCase("Strict_NCP"))
        {
            return "StrictNCP";
        } else if (name.equalsIgnoreCase("Military_Time"))
        {
            return "24H";
        } else if (name.equalsIgnoreCase("Meridiem_Time"))
        {
            return "12H";
        }

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
}
