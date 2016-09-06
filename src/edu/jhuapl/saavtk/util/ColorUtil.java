package edu.jhuapl.saavtk.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class ColorUtil
{
    public enum DefaultColor
    {
        RED (Color.RED),
        PINK (Color.PINK),
        ORANGE (Color.ORANGE),
        YELLOW (Color.YELLOW),
        GREEN (Color.GREEN),
        MAGENTA (Color.MAGENTA),
        CYAN (Color.CYAN),
        BLUE (Color.BLUE),
        WHITE (Color.WHITE),
        LIGHT_GRAY (Color.LIGHT_GRAY),
        GRAY (Color.GRAY),
        DARK_GRAY (Color.DARK_GRAY),
        BLACK (Color.BLACK);

        private final Color color;
        DefaultColor(Color color)
        {
            this.color = color;
        }

        public Color color()
        {
            return color;
        }
    };

    // From http://stackoverflow.com/questions/223971/how-to-generate-spectrum-color-palettes
    static public Color[] generateColors(int n)
    {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
        {
            cols[i] = Color.getHSBColor((float) i / (float) n, 0.85f, 1.0f);
        }
        return cols;
    }

    // Returns a color which is the base color with modified HSL lightness
    // based on value within [minValue, maxValue]
    static public Color scaleLightness(Color base, double value, double minValue, double maxValue)
    {
        float[] baseHSL = getHSLColorComponents(base);
        return scaleLightness(baseHSL, value, minValue, maxValue);
    }

    // Returns a color which is the base color with modified HSL lightness
    // based on value within [minValue, maxValue]
    static public Color scaleLightness(float[] baseHSL, double value, double minValue, double maxValue)
    {
        double effectiveLightness;
        if(minValue >= maxValue)
        {
            effectiveLightness = baseHSL[2];
        }
        else
        {
            // Range considered is lightness +/- lightnessSpan/2 confined in [0,1]
            float lightnessSpan = 0.5f;
            float baseLightness = baseHSL[2];

            // Bound value to be within [minValue, maxValue]
            value = Math.min(Math.max(value, minValue), maxValue);

            // Now map bounded value to effective lightness
            effectiveLightness =
                    lightnessSpan*((value-minValue)/(maxValue-minValue)-0.5f) + baseLightness;
        }

        // Create color with same HS but a scaled L component
        return getHSLColor(baseHSL[0], baseHSL[1], (float)effectiveLightness);
    }

    /**
     * Converts color specification in HSL to a color object.  Note that the
     * saturation in HSL are not the same as that in HSV.
     *
     * Source: http://www.rapidtables.com/convert/color/hsl-to-rgb.htm
     *
     * @param hue Can be any floating-point number. The floor of this number is
     *            subtracted from it and then multiplied by 360 to get a number
     *            in range [0,360)
     * @param saturation Any number in range [0,1]
     * @param lightness Any number in range [0,1]
     * @return Color object whose RGB components are derived from the HSL input
     */
    public static Color getHSLColor(float hue, float saturation, float lightness)
    {
        // Process inputs
        hue = (hue-(float)Math.floor(hue)) * 360.0f;
        saturation = Math.min(Math.max(saturation, 0.0f), 1.0f);
        lightness = Math.min(Math.max(lightness, 0.0f), 1.0f);

        // Perform conversion
        float c = (1.0f - Math.abs(2.0f*lightness - 1.0f)) * saturation;
        float x = c * (1.0f-Math.abs((hue/60.0f) % 2.0f - 1.0f));
        float m = lightness - c/2.0f;

        float rp, gp, bp;
        if(0 <= hue && hue < 60)
        {
            rp = c;
            gp = x;
            bp = 0;
        }
        else if(60 <= hue && hue < 120)
        {
            rp = x;
            gp = c;
            bp = 0;
        }
        else if(120 <= hue && hue < 180)
        {
            rp = 0;
            gp = c;
            bp = x;
        }
        else if(180 <= hue && hue < 240)
        {
            rp = 0;
            gp = x;
            bp = c;
        }
        else if(240 <= hue && hue < 300)
        {
            rp = x;
            gp = 0;
            bp = c;
        }
        else
        {
            rp = c;
            gp = 0;
            bp = x;
        }

        // Return a new color with computed RGB
        return new Color(rp+m, gp+m, bp+m);
    }


    /**
     * Gets the HSL color specification of a color object.  Note that the
     * saturation in HSL are not the same as that in HSV.
     *
     * Source: http://www.rapidtables.com/convert/color/rgb-to-hsl.htm
     *
     * @param color The color object for which to derive HSL components from
     * @return The hsv specification of the input color where the hsv space
     *         is defined as [0,1) x [0,1] x [0,1]
     */
    public static float[] getHSLColorComponents(Color color)
    {
        // Convert RGB components to be in [0,1]
        float rp = ((float)color.getRed())/255.0f;
        float gp = ((float)color.getGreen())/255.0f;
        float bp = ((float)color.getBlue())/255.0f;

        // Start conversion
        float cmax = Math.max(Math.max(rp, gp), bp);
        float cmin = Math.min(Math.min(rp, gp), bp);
        float delta = cmax - cmin;
        float hue, saturation, lightness;

        // Hue calculation
        if(delta == 0)
        {
            hue = 0;
        }
        else if(cmax == rp)
        {
            hue = 60.0f * (((gp-bp)/delta) % 6.0f);
        }
        else if(cmax == gp)
        {
            hue = 60.0f * ((bp-rp)/delta + 2.0f);
        }
        else
        {
            hue = 60.0f * ((rp-gp)/delta + 4.0f);
        }
        hue /= 360.0f;

        // Lightness calculation
        lightness = (cmax + cmin) / 2.0f;

        // Saturation calculation
        if(delta == 0)
        {
            saturation = 0;
        }
        else
        {
            saturation = delta / (1.0f - Math.abs(2.0f*lightness - 1.0f));
        }

        // Package into an array to return to caller
        float[] hsl = new float[3];
        hsl[0] = hue;
        hsl[1] = saturation;
        hsl[2] = lightness;
        return hsl;
    }

    /**
     * Purpose: this icon simply draws a square with its border in black and
     * its inside in the specified color (passed into the constructor).
     * It is used for showing the current color of something.
     */
    public static class ColorIcon implements Icon
    {

        private int width = 12;
        private int height = 12;
        private Color color;

        public ColorIcon(Color color)
        {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setColor(color);
            g2d.fillRect(x +1 ,y + 1,width -2 ,height -2);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(x +1 ,y + 1,width -2 ,height -2);

            g2d.dispose();
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }

        public Color getColor()
        {
            return color;
        }
    }
}
