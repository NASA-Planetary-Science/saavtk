package edu.jhuapl.saavtk.gui.funk;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class PopupButton extends JButton implements ActionListener {

	JPopupMenu menu;

	public PopupButton() {
		super();
	}
	
	public PopupButton(Icon icon) {
		super(icon);
	}

	public PopupButton(String text, Icon icon) {
		super(text, icon);
	}

	public PopupButton(String text) {
		super(text);
	}

	public PopupButton(JPopupMenu menu)
	{
		super();
		this.menu=menu;
	}
	
	public PopupButton(JPopupMenu menu, Icon icon) {
		super(icon);
		this.menu = menu;
		this.addActionListener(this);
	}
	
	public PopupButton(JPopupMenu menu, String text, Icon icon) {
		super(text, icon);
		this.menu = menu;
		this.addActionListener(this);
	}
	
	public PopupButton(JPopupMenu menu, String text) {
		super(text);
		this.menu = menu;
		this.addActionListener(this);
	}

	private PopupButton(Action a) {
		super(a);
	}

	public void setPopupMenu(JPopupMenu menu) {
		this.menu = menu;
	}

	@Override
	public void actionPerformed(ActionEvent e) // convert the button press into a mouse event so popup is properly shown
	{
		if (e.getSource() == this && menu != null) {
			Point p = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(p, (Component) e.getSource());
			menu.show(this, p.x, p.y);
		}
	}

}
