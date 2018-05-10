package edu.jhuapl.saavtk2.geom.euclidean;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Frustum 
{
    // location of the origin of frustum
    protected final Vector3D origin;// = new double[3];

    // vector pointing in upper left of frustum
    protected final Vector3D ul;// = new double[3];

    // vector pointing in upper right of frustum
    protected final Vector3D ur;// = new double[3];

    // vector pointing in lower left of frustum
    protected final Vector3D ll;// = new double[3];

    // vector pointing in lower right of frustum
    protected final Vector3D lr;// = new double[3];
    protected Vector3D boresight;	// unlike the corners of the frustum this is mutable, since it is read in from .info files; we want the value to be consistent with what is on file, not just making the assumption that the boresight is along the central axis of the frustum
    
    protected final Vector3D r;
    protected final Vector3D u;
    
    private final double fovxRad;
    private final double fovyRad;
    
    public Frustum(double[] origin, double[] ul, double[] ur, double[] ll, double [] lr)
    {
    	this(new Vector3D(origin), new Vector3D(ul), new Vector3D(ur), new Vector3D(ll), new Vector3D(lr));
    }
    
    public Frustum(Vector3D origin, Vector3D ul, Vector3D ur, Vector3D ll, Vector3D lr)
    {
        this.origin = origin;
        this.ul = ul.normalize();
        this.ur = ur.normalize();
        this.ll = ll.normalize();
        this.lr = lr.normalize();

        fovxRad = Math.abs(Vector3D.angle(ul, ur));
        fovyRad = Math.abs(Vector3D.angle(ur, lr));
        
        boresight=ul.add(ur).add(ll).add(lr).scalarMultiply(0.25).normalize();
        
        r=ur.subtract(ul).normalize();
        u=ur.subtract(lr).normalize();
        
//    	System.out.println(ul+" "+ur+" "+ll+" "+lr+" "+r+" "+u+" "+boresight);
    }
    
    public Frustum(Vector3D origin, Vector3D lookAt, Vector3D up, double fovxDeg, double fovyDeg)	// +x is boresight cross up, +y is direction of up
    {
    	this.origin=origin;
    	boresight=lookAt.subtract(origin).normalize();
    	Vector3D xhat=boresight.crossProduct(up).normalize();
    	Vector3D yhat=up.normalize();
    	double fovx=Math.toRadians(fovxDeg);
    	double fovy=Math.toRadians(fovyDeg);
    	double dxhf=Math.tan(fovx/2);
    	double dyhf=Math.tan(fovy/2);
    	ul=xhat.scalarMultiply(-dxhf).add(yhat.scalarMultiply(+dyhf)).add(boresight);
    	ur=xhat.scalarMultiply(+dxhf).add(yhat.scalarMultiply(+dyhf)).add(boresight);
    	ll=xhat.scalarMultiply(-dxhf).add(yhat.scalarMultiply(-dyhf)).add(boresight);
    	lr=xhat.scalarMultiply(+dxhf).add(yhat.scalarMultiply(-dyhf)).add(boresight);
    	fovxRad=fovx;
    	fovyRad=fovy;
    	r=ur.subtract(ul).normalize();
    	u=ur.subtract(lr).normalize();
    	
    }
    
    public double getFovXDeg()
    {
    	return Math.toDegrees(fovxRad);
    }
    
    public double getFovYDeg()
    {
    	return Math.toDegrees(fovyRad);
    }
    
    public double getFovXRad()
    {
    	return fovxRad;
    }
    
    public double getFovYRad()
    {
    	return fovyRad;
    }
    
    public Vector3D getU()
    {
    	return u;
    }
    
    public Vector3D getR()
    {
    	return r;
    }
    
    
    /**
     * Given a point in the frustum compute the texture coordinates of the
     * point assuming the frustum represents the field of the view of
     * a camera.
     * @param pt desired point to compute texture coordinates for
     * @param uv returned texture coordinates as a 2 element vector
     */
    public void computeTextureCoordinatesFromPoint(double[] pos, double[] uv, boolean clipToImage)
    {
        Vector3D vec=new Vector3D(pos).normalize();

        double d1 = Vector3D.angle(vec, ul);
        double d2 = Vector3D.angle(vec, lr);

        double v = (d1*d1 + fovyRad*fovyRad - d2*d2) / (2.0*fovyRad);
        double u = d1*d1 - v*v;
        
        if (u <= 0.0)
            u = 0.0;
        else
            u = Math.sqrt(u);

        //System.out.println(v/b + " " + u/a + " " + d1 + " " + d2);

        v = v/fovyRad;
        u = u/fovxRad;

        if (clipToImage)
        {
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
        }

        uv[0] = u;
        uv[1] = v;

        //adjustTextureCoordinates(width, height, uv);
    }

    /**
     * Given an offset point from the image center in texture coordinates, return an offset vector
     * of the point from the image center in world coordinates assuming the frustum represents the
     * field of the view of a camera.
     * @param pt resulting offset vector in world coordinates as a 2 element vector
     * @param uv texture coordinates offset from center of image as a 2 element vector
     */
    public void computeOffsetFromTextureCoordinates(double[] uv, int width, int height, Vector3D offset)
    {
//        readjustTextureCoordinates(width, height, uv);
        Vector3D hvec=ll.subtract(ul);
        Vector3D wvec=ur.subtract(ul);
        double hsize = hvec.getNorm();
        double wsize = wvec.getNorm();
        double dh = uv[1] * hsize / (height - 1);
        double dw = uv[0] * wsize / (width - 1);
        offset=hvec.normalize().scalarMultiply(dh);
        offset=wvec.normalize().scalarMultiply(dw);
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
    
    @Override
    public String toString()
    {
    	return "Frustum [ o="+origin+" ll="+ll+" lr="+lr+" ul="+ul+" ur="+ur+"]";
    }
    
    public Vector3D getBoresightUnit()
    {
    	return boresight;
    }
    
    public Vector3D getOrigin()
    {
    	return origin;
    }
    
    public Vector3D getUpperLeftUnit()
    {
    	return ul;
    }
    
    public Vector3D getUpperRightUnit()
    {
    	return ur;
    }
    
    public Vector3D getLowerLeftUnit()
    {
    	return ll;
    }
    
    public Vector3D getLowerRightUnit()
    {
    	return lr;
    }

/*    public void setBoresight(Vector3D v)
    {
    	boresight=v.normalize();
    }*/
    
/*    @Override
    public double[] computeRawTextureCoordinates(Vector3D position)	// this does not apply the opengl-relevant correction of the static method adjustTextureCoordinates()
    {
    	double[] uv=new double[2];
    	computeTextureCoordinatesFromPoint(position.toArray(), uv, true);
    	return uv;
    }*/
}
