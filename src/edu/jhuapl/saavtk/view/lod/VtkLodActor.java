package edu.jhuapl.saavtk.view.lod;

import java.util.HashMap;
import java.util.Map;

import edu.jhuapl.saavtk.view.AssocActor;
import vtk.vtkActor;
import vtk.vtkMapper;

/**
 * {@link vtkActor} which implements the {@link LodActor} interface and the
 * {@link AssocActor} interface. This vtkActor allows a mapping of
 * {@link vtkMapper}s to be associated with each {@link LodMode} which provides
 * a flexible and robust support of LOD (level of detail).
 * <P>
 * Do <B>not</B> call the method {@link #SetMapper} but rather utilize the
 * methods {@link #setDefaultMapper} or {@link #setLodMapper}. Calling this
 * method will result in a {@link RuntimeException}. This class will internally
 * manage the installed {@link vtkMapper}s.
 *
 * @author lopeznr1
 */
public class VtkLodActor extends vtkActor implements AssocActor, LodActor
{
	// Reference vars
	private final Object refModel;

	// State vars
	private LodMode lodMode;
	private Map<LodMode, vtkMapper> lodModeMapperM;
	private vtkMapper vDefaultMapper;

	/**
	 * Standard Constructor
	 *
	 * @param aModel Object that will be associated with this actor. May be null.
	 */
	public VtkLodActor(Object aModel)
	{
		refModel = aModel;

		lodMode = LodMode.Auto;
		lodModeMapperM = new HashMap<>();
		vDefaultMapper = null;
	}

	/**
	 * Sets the default {@link vtkMapper} to utilize when a {@link LodMode} has been
	 * specified which does not have a corresponding {@link vtkMapper}.
	 * <P>
	 * The passed in value must not be null.
	 */
	public void setDefaultMapper(vtkMapper aMapper)
	{
		if (aMapper == null)
			throw new NullPointerException("Default vtkMapper must not be null!");

		vDefaultMapper = aMapper;

		vtkUpdateState();
	}

	/**
	 * Sets the {@link vtkMapper} to utilize with the specified {@link LodMode}.
	 */
	public void setLodMapper(LodMode aLodMode, vtkMapper aMapper)
	{
		// Set the default vtkMapper to the first one specified (if not yet configured)
		if (aMapper != null && vDefaultMapper == null)
			vDefaultMapper = aMapper;

		lodModeMapperM.put(aLodMode, aMapper);

		vtkUpdateState();
	}

	@Override
	public <G1> G1 getAssocModel(Class<G1> aType)
	{
		if (aType.isInstance(refModel) == true)
			return aType.cast(refModel);

		return null;
	}

	@Override
	public void setLodMode(LodMode aLodMode)
	{
		lodMode = aLodMode;

		vtkUpdateState();
	}

	/**
	 * This method should not be called directly. Instead utilize one of the
	 * following methods:
	 * <UL>
	 * <LI>{@link #setDefaultMapper(vtkMapper)}
	 * <LI>{@link #setLodMapper(LodMode, vtkMapper)}.
	 * </UL>
	 */
	@Deprecated
	@Override
	public void SetMapper(vtkMapper aMapper)
	{
		throw new RuntimeException("API misuse error! This method should not be called.");
	}

	/**
	 * Method to update internal VTK state.
	 */
	private void vtkUpdateState()
	{
		// Bail if no valid vtkMapper
		vtkMapper tmpMapper = lodModeMapperM.getOrDefault(lodMode, vDefaultMapper);
		if (tmpMapper == null)
			return;

		super.SetMapper(tmpMapper);
	}
}
