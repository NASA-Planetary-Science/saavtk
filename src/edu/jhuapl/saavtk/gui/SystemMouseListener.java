package edu.jhuapl.saavtk.gui;

import java.awt.Point;

// to be used with SystemMouse
@FunctionalInterface
public interface SystemMouseListener {
	void mousePositionChanged(Point pos);
}