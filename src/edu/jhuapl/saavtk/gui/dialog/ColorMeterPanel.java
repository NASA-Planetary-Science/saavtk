
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
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.gui.SystemMouse;
import edu.jhuapl.saavtk.gui.SystemMouseListener;

// this color meter panel was adapted from the example at https://stackoverflow.com/questions/13061122/getting-rgb-value-from-under-mouse-cursor 
@SuppressWarnings("serial")
class ColorMeterPanel extends JPanel implements SystemMouseListener, HierarchyListener, AWTEventListener, ChangeListener {

	JLabel hoverColorLabel = new JLabel();
	JLabel screenCaptureLabel = new JLabel()
			{
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (labelDim<screenShotDim)
						labelDim=screenShotDim;
					g.setColor(Color.BLACK);
					((Graphics2D)g).setStroke(new BasicStroke(2));
					int x=labelDim/2-(labelDim/screenShotDim/2)+1;
					int y=labelDim/2-(labelDim/screenShotDim/2)+1;
					int w=labelDim/screenShotDim;
					g.drawRect(x,y,w,w);
					g.setColor(Color.WHITE);
					g.drawRect(x-1, y-1, w+2, w+2);
				}
			};
	JLabel currentColorLabel = new JLabel();
	JColorChooser parent;
	Robot robot;
	ImageIcon icon;
	private int screenShotDim = 21;
	private int labelDim=200;

	public ColorMeterPanel(JColorChooser parent) {
		this.add(screenCaptureLabel);
		this.add(hoverColorLabel);
		this.parent = parent;
		this.addHierarchyListener(this);
		screenCaptureLabel.setPreferredSize(new Dimension(labelDim, labelDim));
		screenCaptureLabel.setOpaque(true);
		screenCaptureLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		hoverColorLabel.setPreferredSize(new Dimension(labelDim, labelDim));
		hoverColorLabel.setOpaque(true);
		hoverColorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		currentColorLabel.setPreferredSize(new Dimension(labelDim, labelDim));
		currentColorLabel.setOpaque(true);
		currentColorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		parent.getSelectionModel().addChangeListener(this);
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mousePositionChanged(Point pos) {
			hoverColorLabel.setBackground(robot.getPixelColor(pos.x, pos.y));
			BufferedImage image = robot.createScreenCapture(new Rectangle(pos.x - screenShotDim / 2, pos.y - screenShotDim / 2, screenShotDim, screenShotDim));
			icon = new ImageIcon(image.getScaledInstance(labelDim, labelDim, BufferedImage.SCALE_DEFAULT));
			screenCaptureLabel.setIcon(icon);
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if (e.getSource() == this) {
			if (isShowing()) {
				SystemMouse.getInstance().addListener(this);
				Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
			} else {
				SystemMouse.getInstance().removeListener(this);
				Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			}
		}
	}

		@Override
		public void eventDispatched(AWTEvent event) {
				KeyEvent keyEvent=(KeyEvent)event;
				if (keyEvent.getKeyCode()==KeyEvent.VK_C)
				{
					Point pos = SystemMouse.getInstance().getPosition();
					parent.setColor(robot.getPixelColor(pos.x, pos.y));
				}
			}

		@Override
		public void stateChanged(ChangeEvent e) {
			currentColorLabel.setBackground(parent.getSelectionModel().getSelectedColor());
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