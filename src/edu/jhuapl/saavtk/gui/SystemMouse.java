package edu.jhuapl.saavtk.gui;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.util.List;

import com.google.common.collect.Lists;

// adapted from https://stackoverflow.com/questions/13061122/getting-rgb-value-from-under-mouse-cursor
public class SystemMouse extends Thread {

	private static final SystemMouse instance=new SystemMouse();
	private static final int sleepPeriod=25;
	
	public static SystemMouse getInstance()
	{
		if (!instance.isAlive())
			instance.start();
		return instance;
	}
	
	private Point lastPoint;
	List<SystemMouseListener> listeners = Lists.newArrayList();

	private SystemMouse() {
			setDaemon(true);
			setPriority(MIN_PRIORITY);
		}

	public void addListener(SystemMouseListener l) {
		listeners.add(l);
	}

	public void removeListener(SystemMouseListener l) {
		listeners.remove(l);
	}

	public Point getPosition() {
		PointerInfo pi = MouseInfo.getPointerInfo();
		return pi.getLocation();
	}

	@Override
	public void run() {
		lastPoint = getPosition();
		while (true) {
			try

			{
				sleep(sleepPeriod);
			} catch (InterruptedException e) {
			}
			
			Point currentPoint = getPosition();
			if (!currentPoint.equals(lastPoint)) {
				for (SystemMouseListener l : listeners)
					l.mousePositionChanged(currentPoint);
				lastPoint = currentPoint;
			}
		}
	}

	

}
