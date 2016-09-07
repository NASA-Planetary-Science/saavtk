package edu.jhuapl.saavtk.example;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;

public class ExamplePolyhedralModel extends GenericPolyhedralModel
{
    public ExamplePolyhedralModel(ViewConfig config)
    {
        super(config,
                new String[] { config.customName },
                new String[] { getModelFilename(config) },
                null,
                null,
                null,
                null,
                null,
                ColoringValueType.CELLDATA,
                false);
    }

    public boolean isBuiltIn()
    {
        return false;
    }

    private static String getModelFilename(ViewConfig config)
    {
        return config.customName;
    }
}
