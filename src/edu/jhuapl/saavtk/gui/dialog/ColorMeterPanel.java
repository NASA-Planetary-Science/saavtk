
package edu.jhuapl.saavtk.gui.dialog;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.gui.SystemMouse;

// this color meter panel was adapted from the example at https://stackoverflow.com/questions/13061122/getting-rgb-value-from-under-mouse-cursor 
@SuppressWarnings("serial")
class ColorMeterPanel extends JPanel implements PropertyChangeListener, HierarchyListener, AWTEventListener, ChangeListener
{

    JLabel hoverColorPatch = new JLabel();
    JLabel screenCapturePatch = new JLabel() {
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            if (labelDim < screenShotDim)
                labelDim = screenShotDim;
            g.setColor(Color.BLACK);
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            int x = labelDim / 2 - (labelDim / screenShotDim / 2) + 1;
            int y = labelDim / 2 - (labelDim / screenShotDim / 2) + 1;
            int w = labelDim / screenShotDim;
            g.drawRect(x, y, w, w);
            g.setColor(Color.WHITE);
            g.drawRect(x - 1, y - 1, w + 2, w + 2);
        }
    };
    JLabel currentColorPatch = new JLabel();
    JColorChooser parent;
    private SystemMouse systemMouse;
    Robot robot;
    ImageIcon icon;
    private int screenShotDim = 21;
    private int labelDim = 100;

    public ColorMeterPanel(JColorChooser parent) throws AWTException
    {
        this.add(screenCapturePatch);
        this.add(hoverColorPatch);
        this.add(currentColorPatch);
        currentColorPatch.setBackground(parent.getSelectionModel().getSelectedColor());

        this.add(new JLabel("<html>Note: hover over any pixel on the screen<br>&nbsp&nbsp&nbsp&nbsp and press 'c' to select its color</html>"));

        this.parent = parent;
        this.addHierarchyListener(this);
        screenCapturePatch.setPreferredSize(new Dimension(labelDim, labelDim));
        screenCapturePatch.setOpaque(true);
        screenCapturePatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        hoverColorPatch.setPreferredSize(new Dimension(labelDim, labelDim));
        hoverColorPatch.setOpaque(true);
        hoverColorPatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        currentColorPatch.setPreferredSize(new Dimension(labelDim, labelDim));
        currentColorPatch.setOpaque(true);
        currentColorPatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        parent.getSelectionModel().addChangeListener(this);
        robot = new Robot();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(SystemMouse.POINTER_POSITION))
        {
            Point pos = (Point) e.getNewValue();
            hoverColorPatch.setBackground(robot.getPixelColor(pos.x, pos.y));
            BufferedImage image = robot.createScreenCapture(new Rectangle(pos.x - screenShotDim / 2, pos.y - screenShotDim / 2, screenShotDim, screenShotDim));
            icon = new ImageIcon(image.getScaledInstance(labelDim, labelDim, BufferedImage.SCALE_DEFAULT));
            screenCapturePatch.setIcon(icon);
        }
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e)
    {
        if (e.getSource() == this)
        {
            if (isShowing())
            {
                if (systemMouse == null)
                {
                    systemMouse = SystemMouse.of();
                    systemMouse.addPropertyChangeListener(this);
                    systemMouse.execute();
                }
                Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
            }
            else
            {
                if (systemMouse != null)
                {
                    systemMouse.removePropertyChangeListener(this);
                    systemMouse.cancel(true);
                    systemMouse = null;
                }
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            }
        }
    }

    @Override
    public void eventDispatched(AWTEvent event)
    {
        KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.getKeyCode() == KeyEvent.VK_C && systemMouse != null)
        {
            Point pos = systemMouse.getPosition();
            parent.setColor(robot.getPixelColor(pos.x, pos.y));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        currentColorPatch.setBackground(parent.getSelectionModel().getSelectedColor());
    }

//	@Override
//	public void keyTyped(KeyEvent e) {
//		System.out.println(e);
//		if (chooseMode && e.isControlDown() && e.getKeyChar() == 'c') {
//			Point pos = SystemMouse.getInstance().getPosition();
//			parent.getColorSelectionModel().setSelectedColor(robot.getPixelColor(pos.x, pos.y));
//			System.out.println(parent.getColorSelectionModel().getSelectedColor());
//			chooseMode = false;
//		}
//
//	}
//
//	@Override
//	public void keyPressed(KeyEvent e) {
//	}
//
//	@Override
//	public void keyReleased(KeyEvent e) {
//		// TODO Auto-generated method stub
//
//	}

}