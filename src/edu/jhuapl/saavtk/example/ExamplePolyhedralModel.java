package edu.jhuapl.saavtk.example;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.PolyhedralModelConfig;

public class ExamplePolyhedralModel extends GenericPolyhedralModel
{
    public ExamplePolyhedralModel(PolyhedralModelConfig config)
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

    private static String getModelFilename(PolyhedralModelConfig config)
    {
        return config.customName;
    }
}
