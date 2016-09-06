package edu.jhuapl.saavtk.gui;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SearchPanelUtil
{
    public static JPanel createFromToPanel(
            JFormattedTextField fromField,
            JFormattedTextField toField,
            double fromValue,
            double toValue,
            String labelTextLeft,
            String labelTextMiddle,
            String labelTextRight
    )
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,
                BoxLayout.LINE_AXIS));

        JLabel fromLabel = new JLabel(labelTextLeft + " ");

        fromField.setValue(fromValue);
        fromField.setMaximumSize(new Dimension(50, 23));
        fromField.setColumns(5);

        JLabel toLabel = new JLabel(" " + labelTextMiddle + " ");

        toField.setValue(toValue);
        toField.setMaximumSize(new Dimension(50, 23));
        toField.setColumns(5);

        JLabel endLabel = new JLabel(" " + labelTextRight);

        panel.add(fromLabel);
        panel.add(fromField);
        panel.add(toLabel);
        panel.add(toField);
        panel.add(endLabel);

        return panel;
    }
}
