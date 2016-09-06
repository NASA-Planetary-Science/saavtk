package edu.jhuapl.saavtk.util;


public class Frustum
{
    // location of the origin of frustum
    public final double[] origin;// = new double[3];

    // vector pointing in upper left of frustum
    public final double[] ul;// = new double[3];

    // vector pointing in upper right of frustum
    public final double[] ur;// = new double[3];

    // vector pointing in lower left of frustum
    public final double[] ll;// = new double[3];

    // vector pointing in lower right of frustum
    public final double[] lr;// = new double[3];

    private final double a;
    private final double b;

    public Frustum(
            double[] origin,
            double[] ul,
            double[] ur,
            double[] ll,
            double[] lr)
    {
        this.origin = origin.clone();
        this.ul = ul.clone();
        this.ur = ur.clone();
        this.ll = ll.clone();
        this.lr = lr.clone();

        a = MathUtil.vsep(ul, ur);
        b = MathUtil.vsep(ul, lr);
    }

    /**
     * Given a point in the frustum compute the texture coordinates of the
     * point assuming the frustum represents the field of the view of
     * a camera.
     * @param pt desired point to compute texture coordinates for
     * @param uv returned texture coordinates as a 2 element vector
     */
    public void computeTextureCoordinatesFromPoint(double[] pt, int width, int height, double[] uv, boolean clipToImage)
    {
        double[] vec = {
             pt[0] - origin[0],
             pt[1] - origin[1],
             pt[2] - origin[2]};

        MathUtil.vhat(vec, vec);

        double d1 = MathUtil.vsep(vec, ul);
        double d2 = MathUtil.vsep(vec, lr);

        double v = (d1*d1 + b*b - d2*d2) / (2.0*b);
        double u = d1*d1 - v*v;
        if (u <= 0.0)
            u = 0.0;
        else
            u = Math.sqrt(u);

        //System.out.println(v/b + " " + u/a + " " + d1 + " " + d2);

        v = v/b;
        u = u/a;

        if (clipToImage)
        {
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
        }

        uv[0] = u;
        uv[1] = v;

        adjustTextureCoordinates(width, height, uv);
    }

    /**
     * Given an offset point from the image center in texture coordinates, return an offset vector
     * of the point from the image center in world coordinates assuming the frustum represents the
     * field of the view of a camera.
     * @param pt resulting offset vector in world coordinates as a 2 element vector
     * @param uv texture coordinates offset from center of image as a 2 element vector
     */
    public void computeOffsetFromTextureCoordinates(double[] uv, int width, int height, double[] offset)
    {
//        readjustTextureCoordinates(width, height, uv);
        double[] center = new double[3];
        MathUtil.midpointBetween(ul, lr, center);
        double[] hvec = new double[3];
        MathUtil.vsub(ll, ul, hvec);
        double[] wvec = new double[3];
        MathUtil.vsub(ur, ul, wvec);
        double[] hnorm = new double[3];
        double[] wnorm = new double[3];
        double hsize = MathUtil.unorm(hvec, hnorm);
        double wsize = MathUtil.unorm(wvec, wnorm);
        double dh = uv[1] * hsize / (height - 1);
        double dw = uv[0] * wsize / (width - 1);
        double[] hdir = new double[3];
        double[] wdir = new double[3];
        MathUtil.vscl(dh, hnorm, hdir);
        MathUtil.vscl(dw, wnorm, wdir);
        MathUtil.vadd(hdir, wdir, offset);
    }

    /**
     * This function adjusts the texture coordinates slightly. The reason for this is
     * that in opengl, a texture coordinate value of, say, 0 (or anywhere along the boundary)
     * corresponds to the outer boundary of the pixels along the image border, not the center of
     * the pixels along the image border. However, when the field of view of the camera is
     * provided in spice instrument kernels, it is assumed that the ray pointing along the boundary
     * of the frustum points to the center of the pixels along the border, not the outer boundary
     * of the pixels.
     *
     * To give an oversimplified example, suppose the image is only 2 pixels by 2 pixels, then
     * a texture coordinate of 0, should really be set to 0.25 and a texture coordinate of 1
     * should really be 0.75. Thus, the texture coordinates need to be squeezed slightly.
     * This function does that and maps the range [0, 1] to [1/(2*width), 1-1/(2*width)]
     * or [1/(2*height), 1-1/(2*height)].
     *
     * @param width
     * @param height
     * @param uv
     */
    public static void adjustTextureCoordinates(int width, int height, double[] uv)
    {
        final double umin = 1.0 / (2.0*height);
        final double umax = 1.0 - umin;
        final double vmin = 1.0 / (2.0*width);
        final double vmax = 1.0 - vmin;

        // We need to map the [0, 1] interval into the [umin, umax] and [vmin, vmax] intervals
        uv[0] = (umax - umin) * uv[0] + umin;
        uv[1] = (vmax - vmin) * uv[1] + vmin;
    }


    /**
     * Inverse of adjustTextureCoordinates()
     *
     * @param width
     * @param height
     * @param uv
     */
    public static void readjustTextureCoordinates(int width, int height, double[] uv)
    {
        final double umin = 1.0 / (2.0*height);
        final double umax = 1.0 - umin;
        final double vmin = 1.0 / (2.0*width);
        final double vmax = 1.0 - vmin;

        // We need to map the [0, 1] interval into the [umin, umax] and [vmin, vmax] intervals
        uv[0] = (uv[0] - umin) / (umax - umin);
        uv[1] = (uv[1] - vmin) / (vmax - vmin);
    }
}
