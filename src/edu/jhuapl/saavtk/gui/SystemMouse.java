package edu.jhuapl.saavtk.gui;

import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingWorker;

// adapted from https://stackoverflow.com/questions/13061122/getting-rgb-value-from-under-mouse-cursor
public class SystemMouse extends SwingWorker<Void, Void>
{
    public static final String POINTER_POSITION = "mousePointerPosition";

    public static SystemMouse of()
    {
        return new SystemMouse();
    }

    private static final int sleepPeriod = 25;

    private final AtomicReference<Point> lastPoint;

    protected SystemMouse()
    {
        this.lastPoint = new AtomicReference<>();
    }

    public Point getPosition()
    {
        return lastPoint.get();
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        while (!isDone())
        {
            EventQueue.invokeAndWait(() -> {
                Point currentPoint = MouseInfo.getPointerInfo().getLocation();
                Point lastPoint = this.lastPoint.getAndSet(currentPoint);
                if (!currentPoint.equals(lastPoint))
                {
                    firePropertyChange(POINTER_POSITION, lastPoint, currentPoint);
                }
            });

            try
            {
                Thread.sleep(sleepPeriod);
            }
            catch (@SuppressWarnings("unused") InterruptedException ignored)
            {
                break;
            }
        }

        return null;
    }

}
