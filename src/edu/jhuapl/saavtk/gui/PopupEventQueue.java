package edu.jhuapl.saavtk.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class PopupEventQueue extends EventQueue {

    private JPopupMenu popup;
    private BasicAction cut;
    private BasicAction copy;
    private BasicAction paste;
    private BasicAction selectAll;

    public PopupEventQueue() {
        //createPopupMenu();
    }

    public void createPopupMenu(JTextComponent text){
        cut = new CutAction("Cut",null);
        copy = new CopyAction("Copy", null);
        paste = new PasteAction("Paste",null);
        selectAll = new SelectAllAction("Select All",null);
        cut.setTextComponent(text);
        copy.setTextComponent(text);
        paste.setTextComponent(text);
        selectAll.setTextComponent(text);

        popup = new JPopupMenu();
        popup.add( cut );
        popup.add( copy );
        popup.add( paste );
        popup.addSeparator();
        popup.add( selectAll );
    }

    public void showPopup(Component parent, MouseEvent me){
        popup.validate();
        popup.show(parent, me.getX(), me.getY());
    }

    @Override
    protected void dispatchEvent(AWTEvent event){
        super.dispatchEvent(event);
        if(!(event instanceof MouseEvent)){
            return;
        }
        MouseEvent me = (MouseEvent)event;
        if(!me.isPopupTrigger()) {
            return;
        }
        if( !(me.getSource() instanceof Component) ) {
            return;
        }
        Component comp = SwingUtilities.getDeepestComponentAt((Component)
                me.getSource(),me.getX(), me.getY());
        if( !(comp instanceof JTextComponent)){
            return;
        }
        if(MenuSelectionManager.defaultManager().getSelectedPath().length > 0){
            return;
        }
        createPopupMenu((JTextComponent)comp);
        showPopup((Component)me.getSource(), me);
    }
    public abstract class BasicAction extends AbstractAction{
        private static final long serialVersionUID = 1L;
        JTextComponent comp;

        public BasicAction(String text, Icon icon) {
            super(text, icon);
            putValue(Action.SHORT_DESCRIPTION, text);
        }
        public void setTextComponent(JTextComponent comp){
            this.comp = comp;
        }
        @Override
        public abstract void actionPerformed(ActionEvent e);
    }
    public class CutAction extends BasicAction {
        private static final long serialVersionUID = 1L;
        public CutAction(String text, Icon icon) {
            super(text, icon);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
        }
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e){
            comp.cut();
        }
        @Override
        public boolean isEnabled(){
            return comp != null && comp.isEditable() && comp.getSelectedText() != null;
        }
    }
    public class CopyAction extends BasicAction{
        private static final long serialVersionUID = 1L;
        public CopyAction(String text, Icon icon){
            super(text,icon);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
        }
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
            comp.copy();
        }
        @Override
        public boolean isEnabled() {
            return comp != null && comp.getSelectedText() != null;
        }
    }
    public class PasteAction extends BasicAction{
        private static final long serialVersionUID = 1L;
        public PasteAction(String text, Icon icon){
            super(text,icon);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
        }
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
            comp.paste();
        }
        @Override
        public boolean isEnabled() {
            Transferable content = Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getContents(null);
            return comp != null && comp.isEnabled() && comp.isEditable()
                    && content.isDataFlavorSupported(DataFlavor.stringFlavor);
        }
    }

    public class SelectAllAction extends BasicAction{
        private static final long serialVersionUID = 1L;
        public SelectAllAction(String text, Icon icon){
            super(text,icon);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl A"));
        }
        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e){
            comp.selectAll();
        }
        @Override
        public boolean isEnabled() {
            return comp != null && comp.isEnabled() && comp.getText().length() > 0
                    && (comp.getSelectedText() == null ||
                    comp.getSelectedText().length() < comp.getText().length());
        }
    }
    public static void main(String[] args) {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push( new PopupEventQueue());
        JTextField field = new JTextField(20);
        JTextField field1 = new JTextField(20);
        JPanel center = new JPanel( new FlowLayout(FlowLayout.LEFT) );

        center.add(new JLabel("cell1:"));
        center.add(field);
        center.add(new JLabel("cell2:"));
        center.add(field1);
        JPanel content = new JPanel( new FlowLayout(FlowLayout.LEFT) );
        content.add( center, BorderLayout.SOUTH );
        JFrame frame = new JFrame("cell copy past program");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(content);
        frame.setSize(550,100);
        frame.setVisible(true);
    }
}
