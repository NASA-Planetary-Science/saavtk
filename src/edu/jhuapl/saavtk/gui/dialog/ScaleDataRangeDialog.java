package edu.jhuapl.saavtk.gui.dialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.colormap.SigFigNumberFormat;
import edu.jhuapl.saavtk.gui.GNumberField;
import edu.jhuapl.saavtk.model.PolyhedralModel;

import net.miginfocom.swing.MigLayout;

/**
 * UI panel that allows the user to configure the coloring range associated with
 * the primary model (priModel). The secondary model (secModel) will be updated
 * to reflect the coloring range of the priModel only if the boolean syncModels
 * is set to true.
 * <P>
 * It is the responsibility of any developer of this dialog ensure that the
 * method {@link #setModelConfiguration} is called with the appropriate
 * arguments. Failure to do so will result in NPEs.
 */
public class ScaleDataRangeDialog extends JDialog implements ActionListener
{
    // Constants
    private static final long serialVersionUID = 1L;

    // State vars
    private PolyhedralModel priModel;
    private PolyhedralModel secModel;
    private boolean syncModels;

    // GUI vars
    private GNumberField minValueNF;
    private GNumberField maxValueNF;
    private JButton applyB;
    private JButton resetB;
    private JButton closeB;

    /**
     * Standard Constructor
     *
     * @param aParent
     *            The parent window of this Dialog.
     */
    public ScaleDataRangeDialog(Window aParent)
    {
        super(aParent);

        priModel = null;
        secModel = null;
        syncModels = false;

        buildGui();
        pack();

        setLocationRelativeTo(aParent);
        setModalityType(JDialog.ModalityType.DOCUMENT_MODAL);
    }

    /**
     * Sets in the models associated with this dialog.
     *
     * @param aPriModel
     *            The primary model that this dialog controls.
     * @param aSecModel
     *            The secondary model that this dialog controls (only if
     *            aSyncModels == true).
     * @param aSyncModels
     *            Boolean that defines whether the body aSecModel should be
     *            updated also.
     */
    public void setModelConfiguration(PolyhedralModel aPriModel, PolyhedralModel aSecModel, boolean aSyncModels)
    {
        Preconditions.checkNotNull(aPriModel);
        Preconditions.checkNotNull(aSecModel);

        priModel = aPriModel;
        secModel = aSecModel;
        syncModels = aSyncModels;
    }

    /**
     * Sets in the model associated with this dialog. The secondary model is not
     * used when configured via this method.
     *
     * @param aPriModel
     *            The primary model that this dialog controls.
     */
    public void setModelConfiguration(PolyhedralModel aPriModel)
    {
        Preconditions.checkNotNull(aPriModel);

        priModel = aPriModel;
        secModel = null;
        syncModels = false;
    }

    @Override
    public void actionPerformed(ActionEvent aEvent)
    {
        Object source = aEvent.getSource();
        if (source == minValueNF || source == maxValueNF)
            updateControlArea();

        if (source == applyB)
            syncModelToGui();

        if (source == resetB)
            doResetAction();

        if (source == closeB)
            setVisible(false);
    }

    @Override
    public void setVisible(boolean aBool)
    {
        if (aBool == true)
            syncGuiToModel();

        super.setVisible(aBool);
    }

    /**
     * Helper method which layouts the panel.
     */
    private void buildGui()
    {
        setLayout(new MigLayout());

        // Set up the action area
        JLabel minValueL = new JLabel("Minimum");
        JLabel maxValueL = new JLabel("Maximum");
        minValueNF = new GNumberField(this);
        minValueNF.setFormat(new SigFigNumberFormat(3));
        minValueNF.setColumns(8);
        maxValueNF = new GNumberField(this);
        maxValueNF.setFormat(new SigFigNumberFormat(3));
        maxValueNF.setColumns(8);
        add(minValueL);
        add(minValueNF, "");
        add(maxValueL);
        add(maxValueNF, "wrap");

        // Set up the control area
        applyB = new JButton("Apply");
        applyB.addActionListener(this);
        resetB = new JButton("Reset");
        resetB.addActionListener(this);
        closeB = new JButton("Close");
        closeB.addActionListener(this);
        add(applyB, "align right,span,split");
        add(resetB, "");
        add(closeB, "");
    }

    /**
     * Helper method that will perform the "reset" action
     */
    private void doResetAction()
    {
        // Reset the minValue, maxValue UI elements
        int index = priModel.getColoringIndex();
        double[] defaultArr = priModel.getDefaultColoringRange(index);
        minValueNF.setValue(defaultArr[0]);
        maxValueNF.setValue(defaultArr[1]);

        // Delegate the updating of the model
        syncModelToGui();
    }

    /**
     * Helper method to synchronize the GUI to match the model.
     */
    private void syncGuiToModel()
    {
        int index = priModel.getColoringIndex();
        setTitle("Rescale Range of " + priModel.getColoringName(index));

        double[] tmpArr = priModel.getCurrentColoringRange(index);
        minValueNF.setValue(tmpArr[0]);
        maxValueNF.setValue(tmpArr[1]);
    }

    /**
     * Helper method to synchronize the model to match the GUI.
     */
    private void syncModelToGui()
    {
        double[] tmpArr = { minValueNF.getValue(), maxValueNF.getValue() };

        int index = priModel.getColoringIndex();
        try
        {
            priModel.setCurrentColoringRange(index, tmpArr);
            if (syncModels == true && secModel != null)
                secModel.setCurrentColoringRange(index, tmpArr);
        }
        catch (IOException aExp)
        {
            // Dump to console
            aExp.printStackTrace();
        }

        // Reset the NumberFields in case the requested range scale change was not
        // fully fulfilled (e.g. the max was too high or the min was too low)
        tmpArr = priModel.getCurrentColoringRange(index);
        minValueNF.setValue(tmpArr[0]);
        maxValueNF.setValue(tmpArr[1]);
    }

    /**
     * Helper method to keep the control area properly synchronized to reflect user input.
     */
    private void updateControlArea()
    {
        boolean isEnabled = true;
        isEnabled &= minValueNF.isValidInput();
        isEnabled &= maxValueNF.isValidInput();
        isEnabled &= minValueNF.getValue() < maxValueNF.getValue();
        applyB.setEnabled(isEnabled);
    }
}
