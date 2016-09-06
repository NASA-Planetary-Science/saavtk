package edu.jhuapl.saavtk.model;

import edu.jhuapl.saavtk.query.QueryBase;

public class ImagingInstrument
{
    public SpectralMode spectralMode;
    public QueryBase searchQuery;
    public ImageSource[] searchImageSources;
    public ImageType type;
    public Instrument instrumentName;
    public double rotation;
    public String flip;

    public ImagingInstrument()
    {
        this(SpectralMode.MONO, null, null, null, null, 0.0, "None");
    }
    public ImagingInstrument(double rotation, String flip)
    {
        this(SpectralMode.MONO, null, ImageType.GENERIC_IMAGE, null, null, rotation, flip);
    }

//    public ImagingInstrument(ImageType type, Instrument instrumentName)
//    {
//        this(SpectralMode.MONO, null, type, null, instrumentName, 0.0, "None");
//    }

//    public ImagingInstrument(SpectralMode spectralMode)
//    {
//        this(spectralMode, null, null, null, null, 0.0, "None");
//    }

    public ImagingInstrument(SpectralMode spectralMode, QueryBase searchQuery, ImageType type, ImageSource[] searchImageSources, Instrument instrumentName)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, 0.0, "None");
    }

    public ImagingInstrument(SpectralMode spectralMode, QueryBase searchQuery, ImageType type, ImageSource[] searchImageSources, Instrument instrumentName, double rotation, String flip)
    {
        this.spectralMode = spectralMode;
        this.searchQuery = searchQuery;
        this.type = type;
        this.searchImageSources = searchImageSources;
        this.instrumentName = instrumentName;
        this.rotation = rotation;
        this.flip = flip;
    }

    public ImagingInstrument clone()
    {
        return new ImagingInstrument(spectralMode, searchQuery.clone(), type, searchImageSources.clone(), instrumentName, rotation, flip);
    }
}

