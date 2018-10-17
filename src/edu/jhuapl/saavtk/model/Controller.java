package edu.jhuapl.saavtk.model;

import java.awt.Component;

public interface Controller<M extends Controller.Model, V extends Controller.View>
{

	interface Model
	{

	}

	interface View
	{
		Component getComponent();
	}

	M getModel();

	V getView();
}
