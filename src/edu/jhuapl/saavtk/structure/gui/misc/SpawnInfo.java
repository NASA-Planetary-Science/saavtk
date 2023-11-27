package edu.jhuapl.saavtk.structure.gui.misc;

import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureType;

/**
 * Record that defines the attributes needed to create a structure.
 *
 * @author lopeznr1
 */
public record SpawnInfo(int minSteps, boolean hasMaxSteps)
{
	/** Simplified Constructor */
	public SpawnInfo(int aMinSteps)
	{
		this(aMinSteps, true);
	}

	/**
	 * Utility method that returns the {@link SpawnInfo} corresponding to the specified {@link Structure}.
	 */
	public static SpawnInfo of(Structure aStructure)
	{
		if (aStructure == null)
			return null;

		return of(aStructure.getType());
	}

	/**
	 * Utility method that returns the {@link SpawnInfo} corresponding to the specified {@link StructureType}.
	 */
	public static SpawnInfo of(StructureType aType)
	{
		if (aType == StructureType.Point)
			return new SpawnInfo(1);
		else if (aType == StructureType.Circle)
			return new SpawnInfo(3);
		else if (aType == StructureType.Ellipse)
			return new SpawnInfo(3);
		else if (aType == StructureType.Path)
			return new SpawnInfo(2, false);
		else if (aType == StructureType.Polygon)
			return new SpawnInfo(3, false);

		throw new Error("Unsupported type: " + aType);
	}

}
