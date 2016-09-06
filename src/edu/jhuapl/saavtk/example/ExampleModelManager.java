package edu.jhuapl.saavtk.example;

import java.beans.PropertyChangeListener;

import edu.jhuapl.saavtk.model.AbstractModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;

public class ExampleModelManager extends AbstractModelManager implements PropertyChangeListener
{
    public ExampleModelManager(PolyhedralModel mainModel)
    {
        super(mainModel);
    }
}
