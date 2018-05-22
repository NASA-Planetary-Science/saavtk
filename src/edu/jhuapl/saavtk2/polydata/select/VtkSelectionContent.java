package edu.jhuapl.saavtk2.polydata.select;

public enum VtkSelectionContent	// cf. vtkSelectionNode::SelectionContent
{
	@Deprecated SELECTIONS,
	GLOBALIDS,
	PEDIGREEIDS,
	VALUES,
	INDICES,
	FRUSTUM,
	LOCATIONS,
	THRESHOLDS,
	BLOCKS,
	QUERY;
}
