package edu.jhuapl.saavtk.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import edu.jhuapl.saavtk.model.Model;

public class OpacityChanger extends JDialog implements ChangeListener
{
    private JLabel opacityLabel;
    private JSpinner opacitySpinner;
    private JButton btnNewButton;
    private Model model;

    public OpacityChanger(Model model)
    {
        this.model = model;

        opacityLabel = new JLabel("Opacity");
        opacitySpinner = new JSpinner(new SpinnerNumberModel(model.getOpacity(), 0.0, 1.0, 0.1));
        opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "0.00"));
        opacitySpinner.setPreferredSize(new Dimension(80, 21));
        opacitySpinner.addChangeListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "[]", "[][][]"));

        panel.add(opacityLabel, "cell 0 0");
        panel.add(opacitySpinner, "cell 0 0");

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        getContentPane().add(panel, BorderLayout.CENTER);

        btnNewButton = new JButton("Close");
        btnNewButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });
        panel.add(btnNewButton, "cell 0 1,alignx center");
        pack();
    }

    public void stateChanged(ChangeEvent e)
    {
        double val = (Double)opacitySpinner.getValue();
        model.setOpacity(val);
    }

}
