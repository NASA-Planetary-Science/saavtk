package edu.jhuapl.saavtk2.image.keys;

import com.google.common.base.Objects;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;


/**
 * An ImageKey should be used to uniquely distinguish one image from another. It
 * also contains metadata about the image that may be necessary to know before
 * the image is loaded, such as the image projection information and type of
 * instrument used to generate the image.
 *
 * No two images will have the same values for the fields of this class.
 */
public abstract class BandKey
{
    // The path of the image as passed into the constructor. This is not the
    // same as fullpath but instead corresponds to the name needed to download
    // the file from the server (excluding the hostname and extension).
    protected String tag;
	protected String imageFileName;
    protected String projectionFileName;
    protected GenericPolyhedralModel bodyModel;

    public BandKey(String tag)
    {
    	this.tag=tag;
    }

    public String getTag()
	{
		return tag;
	}
    
    public abstract String getImageFileName();
    public abstract String getProjectionFileName();
    
/*    public GenericPolyhedralModel getSmallBodyModel()
    {
        return bodyModel;
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bodyModel == null) ? 0 : bodyModel.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageBandKey other = (ImageBandKey) obj;
		if (bodyModel == null)
		{
			if (other.bodyModel != null)
				return false;
		}
		else if (!bodyModel.equals(other.bodyModel))
			return false;
		if (tag == null)
		{
			if (other.tag != null)
				return false;
		}
		else if (!tag.equals(other.tag))
			return false;
		return true;
	}

*/

/*    public ImageSource source;

    public FileType fileType;

    public ImagingInstrument instrument;

    public ImageType imageType;

    public String band;

    public int slice;

    public ImageKey(String name, ImageSource source)
    {
        this(name, source, null, null, null, null, 0);
    }

    public ImageKey(String name, ImageSource source,
            ImagingInstrument instrument)
    {
        this(name, source, null, null, instrument, null, 0);
    }

    public ImageKey(String name, ImageSource source, FileType fileType,
            ImageType imageType, ImagingInstrument instrument, String band,
            int slice)
    {
        this.imageFileName = name;
        this.source = source;
        this.fileType = fileType;
        this.imageType = imageType;
        this.instrument = instrument;
        this.band = band;
        this.slice = slice;
        if (instrument != null)
            this.imageType = instrument.type;
    }

    @Override
    public boolean equals(Object obj)
    {
        return imageFileName.equals(((ImageKey) obj).imageFileName)
                && source.equals(((ImageKey) obj).source)
        // && fileType.equals(((ImageKey)obj).fileType)
        ;
    }

    @Override
    public int hashCode()
    {
        return imageFileName.hashCode();
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "{" + imageFileName + "," + source + ","
                + fileType + "," + imageType + "," + instrument + "," + band
                + "," + slice + "," + instrument.type + "}";
    }*/
    
}
