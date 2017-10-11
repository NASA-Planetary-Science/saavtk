package edu.jhuapl.saavtk.util.file;

import edu.jhuapl.saavtk.util.SafePaths;

public class FileLocators
{
    public static FileLocator replaceSuffix(final String suffix)
    {
        return new FileLocator()
        {
            @Override
            public String getLocation(String name)
            {
                if (name.contains("."))
                {                    
                	return name.replaceAll("\\.[^\\.]*$", suffix);
                }
                return name + suffix;
            }
        };
    }

    public static FileLocator prependPrefix(final String prefix)
    {
        return new FileLocator()
        {
            @Override
            public String getLocation(String name)
            {
                return SafePaths.getString(prefix, name);
            }
        };
    }

    public static FileLocator concatenate(final FileLocator locator0, final FileLocator... locators)
    {
        if (locators.length == 0) return locator0;
        return new FileLocator()
        {
			@Override
			public String getLocation(String name) {
			    String result = name;
			    for (int index = locators.length - 1; index >= 0; --index)
			    {
			        result = locators[index].getLocation(result);
			    }
			    return locator0.getLocation(result);
			}
        };
    }
}
