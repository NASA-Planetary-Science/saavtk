package edu.jhuapl.saavtk.util;

public class IntensityRange
{
    public final int min;
    public final int max;

    public IntensityRange(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString()
    {
        return "[" + min + ", " + max + "]";
    }
}
