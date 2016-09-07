package edu.jhuapl.saavtk.gui.jogl;


public class StereoCapableMirrorCanvas extends MirrorCanvas
{

    StereoMode mode=StereoMode.ANAGLYPH;

    public static enum StereoMode
    {
        NONE,SIDEBYSIDE,ANAGLYPH, SIDEBYSIDEBALANCED;

        public static StereoMode nextMode(StereoMode mode)
        {
            int nextMode=(mode.ordinal()+1)%(StereoMode.values().length);
            return StereoMode.values()[nextMode];
        }
    }

    public StereoCapableMirrorCanvas(vtksbmtJoglCanvas parent, StereoMode initialMode)
    {
        this(parent);
        setMode(initialMode);
    }

    public StereoCapableMirrorCanvas(vtksbmtJoglCanvas parent)
    {
        super(parent);
        getRenderWindow().StereoCapableWindowOn();
        setMode(StereoMode.NONE);
   }

    public void setMode(StereoMode newMode)
    {
        switch (newMode)
        {
        case NONE:
            getRenderWindow().StereoRenderOff();
            break;
        case SIDEBYSIDE:
            getRenderWindow().SetStereoTypeToSplitViewportHorizontal();
            getRenderWindow().StereoRenderOn();
            break;
        case ANAGLYPH:
            getRenderWindow().SetStereoTypeToAnaglyph();
            getRenderWindow().StereoRenderOn();
            break;
        case SIDEBYSIDEBALANCED:
            getRenderWindow().SetStereoTypeToSplitViewportHorizontal();
            getRenderWindow().StereoRenderOff();
            break;
        }
        Render();
    }

    public StereoMode getMode()
    {
        return mode;
    }

}
