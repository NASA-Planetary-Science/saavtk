package edu.jhuapl.saavtk.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import net.miginfocom.swing.MigLayout;

/**
 * The purpose of this dialog is to change
 */
public class ChangeLatLonDialog<G1 extends Structure> extends JDialog implements ActionListener
{
	// Ref vars
	private AbstractEllipsePolygonModel refManager;
	private Ellipse refItem;

	// Gui vars
	private JButton applyButton;
	private JButton okayButton;
	private JButton cancelButton;
	private JFormattedTextField latTextField;
	private JFormattedTextField lonTextField;

	/**
	 * Standard Constructor
	 *
	 * @param aManager Reference StructureManager of type
	 *                 {@link AbstractEllipsePolygonModel}.
	 * @param aItem    Reference structure of type {@link Ellipse}
	 */
	public ChangeLatLonDialog(StructureManager<G1> aManager, G1 aItem)
	{
		refManager = (AbstractEllipsePolygonModel) aManager;
		refItem = (Ellipse) aItem;

		setTitle("Change Latitude/Longitude");

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(6);

		JLabel latLabel = new JLabel("Latitude (deg)");
		latTextField = new JFormattedTextField(nf);
		latTextField.setPreferredSize(new Dimension(125, 23));
		JLabel lonLabel = new JLabel("Longitude (deg)");
		lonTextField = new JFormattedTextField(nf);
		lonTextField.setPreferredSize(new Dimension(125, 23));

		JPanel buttonPanel = new JPanel(new MigLayout());
		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		okayButton = new JButton("OK");
		okayButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(applyButton);
		buttonPanel.add(okayButton);
		buttonPanel.add(cancelButton);

		panel.add(latLabel);
		panel.add(latTextField);
		panel.add(lonLabel);
		panel.add(lonTextField, "wrap");

		panel.add(buttonPanel, "span, align right");

		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

		add(panel, BorderLayout.CENTER);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == applyButton || e.getSource() == okayButton)
		{
			double latitude, longitude;
			try
			{
				latitude = Double.parseDouble(latTextField.getText());
				longitude = Double.parseDouble(lonTextField.getText());
			}
			catch (NumberFormatException ex)
			{
				return;
			}

			refManager.movePolygon(refItem, (Math.PI / 180.0) * latitude, (Math.PI / 180.0) * longitude);

			double[] center = refManager.getCenter(refItem).toArray();

			LatLon ll = MathUtil.reclat(center);

			latitude = ll.lat;
			longitude = ll.lon;
			if (longitude < 0.0)
				longitude += 2.0 * Math.PI;

			// Reset the text fields in case the requested lat/lon change was not
			// fully fulfilled.
			latTextField.setValue((180.0 / Math.PI) * latitude);
			lonTextField.setValue((180.0 / Math.PI) * longitude);
		}

		if (e.getSource() == okayButton || e.getSource() == cancelButton)
		{
			super.setVisible(false);
		}
	}

	@Override
	public void setVisible(boolean aBool)
	{
		double[] center = refManager.getCenter(refItem).toArray();

		LatLon ll = MathUtil.reclat(center);

		double latitude = ll.lat;
		double longitude = ll.lon;
		if (longitude < 0.0)
			longitude += 2.0 * Math.PI;

		latTextField.setValue((180.0 / Math.PI) * latitude);
		lonTextField.setValue((180.0 / Math.PI) * longitude);

		super.setVisible(aBool);
	}
}
