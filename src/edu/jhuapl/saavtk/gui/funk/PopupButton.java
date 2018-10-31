package edu.jhuapl.saavtk.gui.funk;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class PopupButton extends JButton implements ActionListener {

	JPopupMenu menu=null;

	public PopupButton() {
		super();
		this.menu=new JPopupMenu();
		this.addActionListener(this);
	}
	
	public PopupButton(Icon icon) {
		super(icon);
		this.menu=new JPopupMenu();
		this.addActionListener(this);
	}

	public PopupButton(String text, Icon icon) {
		super(text, icon);
		this.menu=new JPopupMenu();
		this.addActionListener(this);
	}

	public PopupButton(String text) {
		super(text);
		this.menu=new JPopupMenu();
		this.addActionListener(this);
	}

	public PopupButton(JPopupMenu menu)
	{
		super();
		this.menu=menu;
		this.addActionListener(this);
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

	public void setPopup(JPopupMenu menu) {
		this.menu = menu;
	}
	
	public JPopupMenu getPopup()
	{
		return menu;
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
