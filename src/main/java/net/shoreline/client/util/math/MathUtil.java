package net.shoreline.client.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author linus
 * @since 1.0
 */
public class MathUtil
{
    private static final int EXP_INT_TABLE_MAX_INDEX = 750;
    private static final int EXP_INT_TABLE_LEN = EXP_INT_TABLE_MAX_INDEX * 2;
    private static final int EXP_FRAC_TABLE_LEN = 1025;
    private static final double[] FACT = new double[]
            {
                    1.0d,                        // 0
                    1.0d,                        // 1
                    2.0d,                        // 2
                    6.0d,                        // 3
                    24.0d,                       // 4
                    120.0d,                      // 5
                    720.0d,                      // 6
                    5040.0d,                     // 7
                    40320.0d,                    // 8
                    362880.0d,                   // 9
                    3628800.0d,                  // 10
                    39916800.0d,                 // 11
                    479001600.0d,                // 12
                    6227020800.0d,               // 13
                    87178291200.0d,              // 14
                    1307674368000.0d,            // 15
                    20922789888000.0d,           // 16
                    355687428096000.0d,          // 17
                    6402373705728000.0d,         // 18
                    121645100408832000.0d,       // 19
            };

    // ANCIENT TECHNOLOGY
    public static double round(double value, int places)
    {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Compute exp(p) for an integer p in extended precision.
     *
     * @param p      integer whose exponential is requested
     * @param result placeholder where to put the result in extended precision
     * @return exp(p) in standard precision (equal to result[0] + result[1])
     */
    private static double expint(int p, double[] result)
    {
        final double[] xs = new double[2];
        final double[] as = new double[2];
        final double[] ys = new double[2];
        xs[0] = 2.718281828459045;
        xs[1] = 1.4456468917292502E-16;
        split(1.0, ys);
        while (p > 0)
        {
            if ((p & 1) != 0)
            {
                quadMult(ys, xs, as);
                ys[0] = as[0];
                ys[1] = as[1];
            }
            quadMult(xs, xs, as);
            xs[0] = as[0];
            xs[1] = as[1];
            p >>= 1;
        }
        if (result != null)
        {
            result[0] = ys[0];
            result[1] = ys[1];
            resplit(result);
        }
        return ys[0] + ys[1];
    }

    /**
     * @param x
     * @param result
     * @return
     */
    public static double slowexp(final double x, final double[] result)
    {
        final double[] xs = new double[2];
        final double[] ys = new double[2];
        final double[] facts = new double[2];
        final double[] as = new double[2];
        split(x, xs);
        ys[0] = ys[1] = 0.0;
        for (int i = FACT.length - 1; i >= 0; i--)
        {
            splitMult(xs, ys, as);
            ys[0] = as[0];
            ys[1] = as[1];
            split(FACT[i], as);
            splitReciprocal(as, facts);
            splitAdd(ys, facts, as);
            ys[0] = as[0];
            ys[1] = as[1];
        }
        if (result != null)
        {
            result[0] = ys[0];
            result[1] = ys[1];
        }
        return ys[0] + ys[1];
    }

    /**
     * @param a
     * @param b
     * @param result
     */
    private static void quadMult(final double[] a,
                                 final double[] b,
                                 final double[] result)
    {
        final double[] xs = new double[2];
        final double[] ys = new double[2];
        final double[] zs = new double[2];
        split(a[0], xs);
        split(b[0], ys);
        splitMult(xs, ys, zs);
        result[0] = zs[0];
        result[1] = zs[1];
        split(b[1], ys);
        splitMult(xs, ys, zs);
        double tmp = result[0] + zs[0];
        result[1] -= tmp - result[0] - zs[0];
        result[0] = tmp;
        tmp = result[0] + zs[1];
        result[1] -= tmp - result[0] - zs[1];
        result[0] = tmp;
        split(a[1], xs);
        split(b[0], ys);
        splitMult(xs, ys, zs);
        tmp = result[0] + zs[0];
        result[1] -= tmp - result[0] - zs[0];
        result[0] = tmp;
        tmp = result[0] + zs[1];
        result[1] -= tmp - result[0] - zs[1];
        result[0] = tmp;
        split(a[1], xs);
        split(b[1], ys);
        splitMult(xs, ys, zs);
        tmp = result[0] + zs[0];
        result[1] -= tmp - result[0] - zs[0];
        result[0] = tmp;
        tmp = result[0] + zs[1];
        result[1] -= tmp - result[0] - zs[1];
        result[0] = tmp;
    }

    /**
     * @param a
     * @param b
     * @param ans
     */
    private static void splitMult(double[] a, double[] b, double[] ans)
    {
        ans[0] = a[0] * b[0];
        ans[1] = a[0] * b[1] + a[1] * b[0] + a[1] * b[1];
        resplit(ans);
    }

    /**
     * Compute split[0], split[1] such that their sum is equal to d,
     * and split[0] has its 30 least significant bits as zero.
     *
     * @param d     number to split
     * @param split placeholder where to place the result
     */
    private static void split(final double d, final double[] split)
    {
        if (d < 8e298 && d > -8e298)
        {
            final double a = d * 0x40000000L;
            split[0] = (d + a) - a;
            split[1] = d - split[0];
        }
        else
        {
            final double a = d * 9.31322574615478515625E-10;
            split[0] = (d + a - d) * 0x40000000L;
            split[1] = d - split[0];
        }
    }

    /**
     * Recompute a split.
     *
     * @param a input/out array containing the split, changed
     *          on output
     */
    private static void resplit(final double[] a)
    {
        final double c = a[0] + a[1];
        final double d = -(c - a[0] - a[1]);
        if (c < 8e298 && c > -8e298)
        {
            double z = c * 0x40000000L;
            a[0] = (c + z) - z;
            a[1] = c - a[0] + d;
        }
        else
        {
            double z = c * 9.31322574615478515625E-10;
            a[0] = (c + z - c) * 0x40000000L;
            a[1] = c - a[0] + d;
        }
    }

    /**
     * @param a
     * @param b
     * @param ans
     */
    private static void splitAdd(final double[] a,
                                 final double[] b,
                                 final double[] ans)
    {
        ans[0] = a[0] + b[0];
        ans[1] = a[1] + b[1];
        resplit(ans);
    }

    /**
     * @param in
     * @param result
     */
    private static void splitReciprocal(final double[] in,
                                        final double[] result)
    {
        final double b = 1.0 / 4194304.0;
        final double a = 1.0 - b;
        if (in[0] == 0.0)
        {
            in[0] = in[1];
            in[1] = 0.0;
        }
        result[0] = a / in[0];
        result[1] = (b * in[0] - a * in[1]) / (in[0] * in[0] + in[0] * in[1]);
        if (result[1] != result[1])
        {
            result[1] = 0.0;
        }
        resplit(result);
        for (int i = 0; i < 2; i++)
        {
            double err = 1.0 - result[0] * in[0] - result[0] * in[1] -
                    result[1] * in[0] - result[1] * in[1];
            err *= result[0] + result[1];
            result[1] += err;
        }
    }

    /**
     * Internal helper method for exponential function.
     *
     * @param x The argument of the exponential function
     * @return exp(x)
     */
    private static double exp(double x)
    {
        double intPartA;
        double intPartB;
        int intVal = (int) x;
        if (x < 0.0)
        {
            if (x < -746d)
            {
                return 0.0;
            }
            if (intVal < -709)
            {
                // This will produce a subnormal output
                return exp(x + 40.19140625) / 285040095144011776.0;
            }
            if (intVal == -709)
            {
                // exp(1.494140625) is nearly a machine number
                return exp(x + 1.494140625) / 4.455505956692756620;
            }
            intVal--;
        }
        else
        {
            if (intVal > 709)
            {
                return Double.POSITIVE_INFINITY;
            }
        }
        intPartA = ExpIntTable.EXP_INT_TABLE_A[EXP_INT_TABLE_MAX_INDEX + intVal];
        intPartB = ExpIntTable.EXP_INT_TABLE_B[EXP_INT_TABLE_MAX_INDEX + intVal];
        final int intFrac = (int) ((x - intVal) * 1024.0);
        final double fracPartA = ExpFracTable.EXP_FRAC_TABLE_A[intFrac];
        final double fracPartB = ExpFracTable.EXP_FRAC_TABLE_B[intFrac];
        final double epsilon = x - (intVal + intFrac / 1024.0);
        double z = 0.04168701738764507;
        z = z * epsilon + 0.1666666505023083;
        z = z * epsilon + 0.5000000000042687;
        z = z * epsilon + 1.0;
        z = z * epsilon + -3.940510424527919E-20;
        double tempA = intPartA * fracPartA;
        double tempB = intPartA * fracPartB + intPartB * fracPartA + intPartB * fracPartB;
        final double tempC = tempB + tempA;
        if (tempC == Double.POSITIVE_INFINITY)
        {
            return Double.POSITIVE_INFINITY;
        }
        return tempC * z + tempB + tempA;
    }

    private static class ExpIntTable
    {
        //
        private static final double[] EXP_INT_TABLE_A;
        private static final double[] EXP_INT_TABLE_B;

        static
        {
            EXP_INT_TABLE_A = new double[EXP_INT_TABLE_LEN];
            EXP_INT_TABLE_B = new double[EXP_INT_TABLE_LEN];
            final double[] tmp = new double[2];
            final double[] recip = new double[2];
            for (int i = 0; i < EXP_INT_TABLE_MAX_INDEX; i++)
            {
                expint(i, tmp);
                EXP_INT_TABLE_A[i + EXP_INT_TABLE_MAX_INDEX] = tmp[0];
                EXP_INT_TABLE_B[i + EXP_INT_TABLE_MAX_INDEX] = tmp[1];
                if (i != 0)
                {
                    // Negative integer powers
                    splitReciprocal(tmp, recip);
                    EXP_INT_TABLE_A[EXP_INT_TABLE_MAX_INDEX - i] = recip[0];
                    EXP_INT_TABLE_B[EXP_INT_TABLE_MAX_INDEX - i] = recip[1];
                }
            }
        }
    }

    private static class ExpFracTable
    {
        private static final double[] EXP_FRAC_TABLE_A;
        private static final double[] EXP_FRAC_TABLE_B;

        static
        {
            EXP_FRAC_TABLE_A = new double[EXP_FRAC_TABLE_LEN];
            EXP_FRAC_TABLE_B = new double[EXP_FRAC_TABLE_LEN];
            final double[] tmp = new double[2];
            final double factor = 1d / (EXP_FRAC_TABLE_LEN - 1);
            for (int i = 0; i < EXP_FRAC_TABLE_A.length; i++)
            {
                slowexp(i * factor, tmp);
                EXP_FRAC_TABLE_A[i] = tmp[0];
                EXP_FRAC_TABLE_B[i] = tmp[1];
            }
        }
    }
}
