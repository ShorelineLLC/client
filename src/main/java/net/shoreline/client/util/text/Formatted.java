package net.shoreline.client.util.text;

import java.text.DecimalFormat;

public interface Formatted
{
    DecimalFormat WHOLE = new DecimalFormat("0");
    DecimalFormat DECIMAL = new DecimalFormat("0.0");
    DecimalFormat DECIMAL_TRIMMED = new DecimalFormat("0.0#");
}
