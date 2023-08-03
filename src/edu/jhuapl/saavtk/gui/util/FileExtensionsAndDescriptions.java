package edu.jhuapl.saavtk.gui.util;

public enum FileExtensionsAndDescriptions
{
	TEXT(".txt", "Text File"), //
	SOURCE_FILE(".txt", "Liger projectile source file"), // 
	PDF(".pdf", "PDF Document"), //
	LIGER(".liger", "Liger File"), //
	SHAPE(".shp", "Shape File")//
	;

	private final String extension;
	private final String description;

	private FileExtensionsAndDescriptions(String extension, String description)
	{
		this.extension = extension;
		this.description = description;
	}

	public String getExtension()
	{
		return this.extension;
	}

	public String getDescription(boolean includeExt)
	{
		if (includeExt)
		{
			return this.description + " (" + this.extension + ")";
		}
		return this.description;
	}
}
